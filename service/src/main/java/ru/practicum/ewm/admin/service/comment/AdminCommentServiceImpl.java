package ru.practicum.ewm.admin.service.comment;

import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.admin.dto.NewParamCommentDto;
import ru.practicum.ewm.base.dto.comment.CommentFullDto;
import ru.practicum.ewm.base.dto.comment.UpdateCommentRequest;
import ru.practicum.ewm.base.exceptions.ConflictException;
import ru.practicum.ewm.base.exceptions.NotFoundException;
import ru.practicum.ewm.base.mapper.CommentMapper;
import ru.practicum.ewm.base.models.Comment;
import ru.practicum.ewm.base.repository.comment.CommentRepository;
import ru.practicum.ewm.base.repository.user.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AdminCommentServiceImpl implements AdminCommentService {
    CommentRepository commentRepository;
    UserRepository userRepository;

    private Comment findById(Long comId) {
        return commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment c ID %d not found", comId)));
    }

    private Specification<Comment> getSpecification(NewParamCommentDto params) {
        return (root, query, criteriaBuilder) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();

            if (Objects.nonNull(params.getText()) && !params.getText().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("text")),
                        "%" + params.getText().toLowerCase() + "%"));
            }

            if (Objects.nonNull(params.getEvents()) && !params.getEvents().isEmpty()) {
                predicates.add(root.get("event").get("id").in(params.getEvents()));
            }

            if (Objects.nonNull(params.getUsers()) && !params.getUsers().isEmpty()) {
                predicates.add(root.get("author").get("id").in(params.getUsers()));
            }

            if (Objects.nonNull(params.getRangeStart())) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("created"), params.getRangeStart()));
            }

            if (Objects.nonNull(params.getRangeEnd())) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("created"), params.getRangeEnd()));
            }

            if (Objects.nonNull(params.getPositive())) {
                predicates.add(criteriaBuilder.equal(root.get("positive"), params.getPositive()));
            }

            query.orderBy(criteriaBuilder.asc(root.get("id")));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<CommentFullDto> getComments(NewParamCommentDto params) {
        PageRequest pageRequest = PageRequest.of(params.getFrom() / params.getSize(),
                params.getSize(),
                Sort.by(Sort.Direction.ASC, "id"));

        List<Comment> comments = commentRepository.findAll(getSpecification(params), pageRequest).getContent();

        return CommentMapper.mapToListDto(comments);
    }

    @Override
    @Transactional
    public CommentFullDto update(UpdateCommentRequest request, Long comId) {
        Comment updatedComment = CommentMapper.updateFields(findById(comId), request);

        try {
            updatedComment = commentRepository.save(updatedComment);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(e.getMessage(), e);
        }

        return CommentMapper.mapToDto(updatedComment);
    }

    @Override
    @Transactional
    public void delete(Long comId) {
        Comment comment = findById(comId);
        userRepository.deleteById(comId);
    }
}
