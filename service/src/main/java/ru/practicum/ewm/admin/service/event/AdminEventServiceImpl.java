package ru.practicum.ewm.admin.service.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.admin.dto.NewParamEventDto;
import ru.practicum.ewm.base.dto.event.EventFullDto;
import ru.practicum.ewm.base.dto.event.UpdateEventAdminRequest;
import ru.practicum.ewm.base.enums.States;
import ru.practicum.ewm.base.exceptions.BadRequestException;
import ru.practicum.ewm.base.exceptions.ConflictException;
import ru.practicum.ewm.base.exceptions.NotFoundException;
import ru.practicum.ewm.base.mapper.EventMapper;
import ru.practicum.ewm.base.models.Category;
import ru.practicum.ewm.base.models.Event;
import ru.practicum.ewm.base.repository.category.CategoryRepository;
import ru.practicum.ewm.base.repository.event.EventRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AdminEventServiceImpl implements AdminEventService {

    EventRepository eventRepository;
    CategoryRepository categoryRepository;

    private Event findById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with ID %d not found", eventId)));
    }

    private Category findCategoryById(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with ID %d not found", catId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<EventFullDto> getEvents(NewParamEventDto params) {
        PageRequest pageRequest = PageRequest.of(params.getFrom() / params.getSize(),
                params.getSize(),
                Sort.by(Sort.Direction.ASC, "id"));

        List<Event> events = eventRepository.findAllWithCriteria(pageRequest, params).toList();

        return EventMapper.mapToListDto(events);
    }

    @Override
    @Transactional
    public EventFullDto update(UpdateEventAdminRequest request, Long eventId) {

        if (request.hasEventDate() && request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new BadRequestException("Field: eventDate. Error: The date and time the event is scheduled and" +
                    " cannot be earlier than one hour after the current time. Value:" + request.getEventDate());
        }

        Event findEvent = findById(eventId);

        if (findEvent.getState().equals(States.PUBLISHED)) {
            throw new ConflictException("Cannot update because event is already published");
        } else if (findEvent.getState().equals(States.CANCELED)) {
            throw new ConflictException("Unable to update because event is already cancelled");
        }

        Category category = null;
        if (request.hasCategory() && !findEvent.getCategory().getId().equals(request.getCategory())) {
            category = findCategoryById(request.getCategory());
        }

        Event updatedEvent = EventMapper.updateAdminFields(findEvent, request, category);
        updatedEvent = eventRepository.save(updatedEvent);
        return EventMapper.mapToDto(updatedEvent);
    }
}
