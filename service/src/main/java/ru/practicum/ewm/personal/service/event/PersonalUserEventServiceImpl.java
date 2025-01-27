package ru.practicum.ewm.personal.service.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.base.dto.ParticipationRequestDto;
import ru.practicum.ewm.base.dto.event.*;
import ru.practicum.ewm.base.enums.States;
import ru.practicum.ewm.base.enums.Statuses;
import ru.practicum.ewm.base.exceptions.BadRequestException;
import ru.practicum.ewm.base.exceptions.ConflictException;
import ru.practicum.ewm.base.exceptions.NotFoundException;
import ru.practicum.ewm.base.mapper.EventMapper;
import ru.practicum.ewm.base.mapper.RequestMapper;
import ru.practicum.ewm.base.models.Category;
import ru.practicum.ewm.base.models.Event;
import ru.practicum.ewm.base.models.Request;
import ru.practicum.ewm.base.models.User;
import ru.practicum.ewm.base.repository.CategoryRepository;
import ru.practicum.ewm.base.repository.EventRepository;
import ru.practicum.ewm.base.repository.RequestRepository;
import ru.practicum.ewm.base.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PersonalUserEventServiceImpl implements PersonalUserEventService {
    UserRepository userRepository;
    EventRepository eventRepository;
    CategoryRepository categoryRepository;
    RequestRepository requestRepository;

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with ID %d not found", userId)));
    }

    private Event findByIdAndInitiatorId(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with ID %d, user with ID %d not found", eventId, userId)));
    }

    private Category findCategoryById(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with ID %d not found", catId)));
    }

    private void checkEventDate(LocalDateTime eventDate) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Field: eventDate. Error: The date and time the event is scheduled and" +
                    " cannot be earlier than 2 hours after the current time. Value:" + eventDate);
        }
    }

    @Override
    @Transactional
    public EventFullDto save(Long userId, NewEventDto request) {
        checkEventDate(request.getEventDate());

        Event event = EventMapper.mapToEntity(request, findCategoryById(request.getCategory()), findUserById(userId));
        event = eventRepository.save(event);
        return EventMapper.mapToDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<EventShortDto> getAll(Long userId, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));

        Set<Event> events = eventRepository.findAllByInitiatorId(userId, pageRequest);

        return EventMapper.mapToListShortDto(events);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto get(Long userId, Long eventId) {
        Event events = findByIdAndInitiatorId(eventId, userId);

        return EventMapper.mapToDto(events);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        Event event = findByIdAndInitiatorId(eventId, userId);

        return RequestMapper.mapToListDto(requestRepository.findAllByEventId(eventId));
    }

    @Override
    @Transactional
    public EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event findEvent = findByIdAndInitiatorId(eventId, userId);

        checkEventDate(request.getEventDate());

        if (findEvent.getState().equals(States.PUBLISHED)) {
            throw new ConflictException("Cannot update because event is already published");
        }

        Category category = null;
        if (request.hasCategory() && !findEvent.getCategory().getId().equals(request.getCategory())) {
            category = findCategoryById(request.getCategory());
        }

        Event updatedEvent = EventMapper.updateUserFields(findEvent, request, category);
        updatedEvent = eventRepository.save(updatedEvent);
        return EventMapper.mapToDto(updatedEvent);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        List<ParticipationRequestDto> confirmedRequests = List.of();
        List<ParticipationRequestDto> rejectedRequests = List.of();

        List<Long> requestIds = request.getRequestIds();
        List<Request> requests = requestRepository.findAllById(requestIds);
        Event findEvent = findByIdAndInitiatorId(eventId, userId);
        String status = request.getStatus();

        // Отклонение всех заявок
        if (status.equals(Statuses.REJECTED.toString())) {
            boolean isConfirmedRequestExists = requests.stream()
                    .anyMatch(elem -> elem.getStatus().equals(Statuses.CONFIRMED));
            if (isConfirmedRequestExists) {
                throw new ConflictException("When trying to reject all applications, a confirmed one was found.");
            }
            rejectedRequests = requests.stream()
                    .peek(elem -> elem.setStatus(Statuses.REJECTED))
                    .map(RequestMapper::mapToDto)
                    .collect(Collectors.toList());

        } else if (status.equals(Statuses.CONFIRMED.toString())) {
            Long participantLimit = findEvent.getParticipantLimit();
            Long approvedRequests = findEvent.getConfirmedRequests();
            long availableParticipants = participantLimit - approvedRequests;
            long waitingParticipants = requestIds.size();

            if (participantLimit > 0 && participantLimit.equals(approvedRequests)) {
                throw new ConflictException(String.format("Event %s has reached its bid limit", findEvent.getTitle()));
            }

            if (participantLimit.equals(0L) || (waitingParticipants <= availableParticipants && !findEvent.getRequestModeration())) {
                confirmedRequests = requests.stream()
                        .peek(elem -> {
                            elem.setStatus(checkStatus(elem.getStatus(), Statuses.CONFIRMED,
                                    "Request with ID " + elem.getId() + " has already been confirmed"));
                        })
                        .map(RequestMapper::mapToDto)
                        .toList();

                findEvent.setConfirmedRequests(approvedRequests + waitingParticipants);
            } else {
                confirmedRequests = requests.stream()
                        .limit(availableParticipants)
                        .peek(elem -> {
                            elem.setStatus(checkStatus(elem.getStatus(), Statuses.CONFIRMED,
                                    "Request with ID " + elem.getId() + " has already been confirmed"));
                        })
                        .map(RequestMapper::mapToDto)
                        .toList();

                rejectedRequests = requests.stream()
                        .skip(availableParticipants)
                        .peek(elem -> {
                            elem.setStatus(checkStatus(elem.getStatus(), Statuses.REJECTED,
                                    "Request with ID " + elem.getId() + " has already been rejected"));
                        })
                        .map(RequestMapper::mapToDto)
                        .toList();

                findEvent.setConfirmedRequests((long) confirmedRequests.size());
            }
        } else {
            throw new ConflictException(String.format("When trying to edit requests, the status %s was incorrect.", status));
        }
        eventRepository.flush();
        requestRepository.flush();
        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    private Statuses checkStatus(Statuses entityStatus, Statuses expectedStatus, String message) {
        if (entityStatus.equals(expectedStatus)) {
            throw new ConflictException(message);
        }
        return expectedStatus;
    }
}
