package ru.practicum.ewm.personal.service.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.base.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.base.enums.States;
import ru.practicum.ewm.base.enums.Statuses;
import ru.practicum.ewm.base.exceptions.BadRequestException;
import ru.practicum.ewm.base.exceptions.ConflictException;
import ru.practicum.ewm.base.exceptions.NotFoundException;
import ru.practicum.ewm.base.mapper.RequestMapper;
import ru.practicum.ewm.base.models.Event;
import ru.practicum.ewm.base.models.Request;
import ru.practicum.ewm.base.models.User;
import ru.practicum.ewm.base.repository.event.EventRepository;
import ru.practicum.ewm.base.repository.request.RequestRepository;
import ru.practicum.ewm.base.repository.user.UserRepository;

import java.util.Collection;
import java.util.List;


@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PersonalUserRequestServiceImpl implements PersonalUserRequestService {
    UserRepository userRepository;
    EventRepository eventRepository;
    RequestRepository requestRepository;

    private Request findByIdAndRequesterId(Long requestId, Long userId) {
        return requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with ID %d of user with ID %d not found",
                        requestId, userId)));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with ID %d not found", userId)));
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with ID %d not found", eventId)));
    }

    @Override
    @Transactional
    public ParticipationRequestDto save(Long userId, Long eventId) {
        if (userId == null || eventId == null) {
            throw new BadRequestException("User or event ID parameter not specified");
        }

        Event event = findEventById(eventId);
        User user = findUserById(userId);

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException(String.format("Request for event with ID %d from user with ID %d already exists",
                    eventId, userId));
        }

        if (event.getInitiator().equals(user)) {
            throw new ConflictException("You cannot create a request to participate in your event.");
        }

        if (!event.getState().equals(States.PUBLISHED)) {
            throw new ConflictException("You cannot participate in an unpublished event.");
        }

        if (!event.getParticipantLimit().equals(0L) && event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            throw new ConflictException("The event participation request limit has been reached");
        }

        if (event.getRequestModeration().equals(Boolean.FALSE)) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        Request request = RequestMapper.mapToEntity(event, user);
        request = requestRepository.save(request);
        return RequestMapper.mapToDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ParticipationRequestDto> get(Long userId) {
        User user = findUserById(userId);

        List<Request> requests = requestRepository.findAllByRequesterId(userId);

        return RequestMapper.mapToListDto(requests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto update(Long userId, Long requestId) {
        Request request = findByIdAndRequesterId(requestId, userId);

        request.setStatus(Statuses.CANCELED);
        request = requestRepository.save(request);
        return RequestMapper.mapToDto(request);
    }
}
