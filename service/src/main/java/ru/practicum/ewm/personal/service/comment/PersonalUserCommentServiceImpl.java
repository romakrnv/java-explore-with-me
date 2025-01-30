package ru.practicum.ewm.personal.service.comment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.base.dto.comment.CommentFullDto;
import ru.practicum.ewm.base.dto.comment.NewCommentDto;
import ru.practicum.ewm.base.dto.comment.UpdateCommentRequest;
import ru.practicum.ewm.base.enums.States;
import ru.practicum.ewm.base.exceptions.ConflictException;
import ru.practicum.ewm.base.exceptions.NotFoundException;
import ru.practicum.ewm.base.mapper.CommentMapper;
import ru.practicum.ewm.base.models.Comment;
import ru.practicum.ewm.base.models.Event;
import ru.practicum.ewm.base.models.User;
import ru.practicum.ewm.base.repository.comment.CommentRepository;
import ru.practicum.ewm.base.repository.event.EventRepository;
import ru.practicum.ewm.base.repository.user.UserRepository;

import java.util.Collection;


@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PersonalUserCommentServiceImpl implements PersonalUserCommentService {
    CommentRepository commentRepository;
    UserRepository userRepository;
    EventRepository eventRepository;

    private Comment findById(Long comId) {
        return commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment c ID %d not found", comId)));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User id %d not found", userId)));
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with ID %d not found", eventId)));
    }

    @Override
    public CommentFullDto save(Long userId, Long eventId, NewCommentDto request) {
        User user = findUserById(userId);
        Event event = findEventById(eventId);

        if (!event.getState().equals(States.PUBLISHED)) {
            throw new ConflictException("comment can only be added to a published event");
        }

        Comment comment = CommentMapper.mapToEntity(request, user, event);
        comment = commentRepository.save(comment);
        return CommentMapper.mapToDto(comment);
    }

    @Override
    public Collection<CommentFullDto> getAll(Long userId, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));

        return CommentMapper.mapToListDto(commentRepository.findAllByAuthorId(userId, pageRequest));
    }

    @Override
    public Collection<CommentFullDto> getAllByEvent(Long userId, Long eventId, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        Event event = findEventById(eventId);

        return CommentMapper.mapToListDto(commentRepository.findAllByAuthorIdAndEventId(userId, eventId, pageRequest));
    }

    @Override
    public CommentFullDto getCommentByEvent(Long userId, Long eventId, Long comId) {
        Event event = findEventById(eventId);

        return CommentMapper.mapToDto(commentRepository.findByIdAndAuthorIdAndEventId(comId, userId, eventId));
    }

    @Override
    public CommentFullDto update(Long userId, Long comId, UpdateCommentRequest request) {
        User user = findUserById(userId);
        Comment comment = findById(comId);

        if (!comment.getAuthor().equals(user)) {
            throw new ConflictException("comment can only be edited by its author");
        }

        Comment updatedComment = CommentMapper.updateFields(comment, request);
        updatedComment = commentRepository.save(updatedComment);
        return CommentMapper.mapToDto(updatedComment);
    }

    @Override
    public void delete(Long userId, Long comId) {
        User user = findUserById(userId);
        Comment comment = findById(comId);

        if (!comment.getAuthor().equals(user)) {
            throw new ConflictException("comment can only be deleted by its author");
        }
        commentRepository.delete(comment);
    }
}
