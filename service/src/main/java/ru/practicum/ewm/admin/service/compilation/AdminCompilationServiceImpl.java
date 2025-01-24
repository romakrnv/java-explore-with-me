package ru.practicum.ewm.admin.service.compilation;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.base.dto.compilation.CompilationDto;
import ru.practicum.ewm.base.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.base.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.base.exceptions.ConflictException;
import ru.practicum.ewm.base.exceptions.NotFoundException;
import ru.practicum.ewm.base.mapper.CompilationMapper;
import ru.practicum.ewm.base.models.Compilation;
import ru.practicum.ewm.base.models.Event;
import ru.practicum.ewm.base.repository.CompilationRepository;
import ru.practicum.ewm.base.repository.EventRepository;

import java.util.Set;

@Slf4j
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminCompilationServiceImpl implements AdminCompilationService {

    CompilationRepository compilationRepository;
    EventRepository eventRepository;

    @Autowired
    public AdminCompilationServiceImpl(CompilationRepository compilationRepository, EventRepository eventRepository) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
    }

    private Compilation findById(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Подборка c ID %d не найдена", compId)));
    }

    private Set<Event> findEvents(Set<Long> events) {
        return events == null ? Set.of() : eventRepository.findAllByIdIn(events);
    }

    @Override
    @Transactional
    public CompilationDto save(NewCompilationDto request) {
        Compilation compilation = CompilationMapper.mapToEntity(request, findEvents(request.getEvents()));

        try {
            compilation = compilationRepository.save(compilation);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(String.format("Подборка с заголовком %s уже существует", compilation.getTitle()), e);
        }

        return CompilationMapper.mapToDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto update(UpdateCompilationRequest request, Long compId) {
        Compilation updatedCompilation = CompilationMapper.updateFields(findById(compId), request, findEvents(request.getEvents()));

        try {
            updatedCompilation = compilationRepository.save(updatedCompilation);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(e.getMessage(), e);
        }

        return CompilationMapper.mapToDto(updatedCompilation);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        Compilation compilation = findById(compId);
        compilationRepository.deleteById(compId);
    }
}
