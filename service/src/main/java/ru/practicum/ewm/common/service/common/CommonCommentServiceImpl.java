package ru.practicum.ewm.common.service.common;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.base.dto.comment.CommentFullDto;
import ru.practicum.ewm.base.dto.comment.CommentShortDto;
import ru.practicum.ewm.base.exceptions.NotFoundException;
import ru.practicum.ewm.base.mapper.CommentMapper;
import ru.practicum.ewm.base.models.Comment;
import ru.practicum.ewm.base.repository.comment.CommentRepository;

import java.util.Collection;
import java.util.List;


@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CommonCommentServiceImpl implements CommonCommentService {
    CommentRepository commentRepository;

    private Comment findById(Long comId) {
        return commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment c ID %d not found", comId)));
    }

    @Override
    public Collection<CommentShortDto> getAll(Long eventId, Boolean positive, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "created"));

        List<Comment> comments;
        if (positive != null) {
            comments = commentRepository.findAllByEventIdAndPositive(eventId, positive, pageRequest);
        } else {
            comments = commentRepository.findAllByEventId(eventId, pageRequest);
        }

        return CommentMapper.mapToListShortDto(comments);
    }

    @Override
    public CommentFullDto get(Long comId) {
        return CommentMapper.mapToDto(findById(comId));
    }
}
