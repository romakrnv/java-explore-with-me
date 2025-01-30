package ru.practicum.ewm.admin.service.compilation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
import ru.practicum.ewm.base.repository.compilation.CompilationRepository;
import ru.practicum.ewm.base.repository.event.EventRepository;

import java.util.Set;


@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AdminCompilationServiceImpl implements AdminCompilationService {

    CompilationRepository compilationRepository;
    EventRepository eventRepository;

    private Compilation findById(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Selection with ID %d not found", compId)));
    }

    private Set<Event> findEvents(Set<Long> events) {
        return events == null ? Set.of() : eventRepository.findAllByIdIn(events);
    }

    @Override
    @Transactional
    public CompilationDto save(NewCompilationDto request) {
        Compilation compilation = CompilationMapper.mapToEntity(request, findEvents(request.getEvents()));

        if (compilationRepository.existsByTitle(request.getTitle())) {
            throw new ConflictException(String.format("A selection with the %s heading already exists", compilation.getTitle()));
        }
        compilation = compilationRepository.save(compilation);
        return CompilationMapper.mapToDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto update(UpdateCompilationRequest request, Long compId) {
        Compilation updatedCompilation = CompilationMapper.updateFields(findById(compId), request, findEvents(request.getEvents()));
        updatedCompilation = compilationRepository.save(updatedCompilation);
        return CompilationMapper.mapToDto(updatedCompilation);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        Compilation compilation = findById(compId);
        compilationRepository.deleteById(compId);
    }
}
