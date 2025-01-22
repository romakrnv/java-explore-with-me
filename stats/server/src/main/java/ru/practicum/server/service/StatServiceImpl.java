package ru.practicum.server.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.server.mapper.EndpointHitMapper;
import ru.practicum.server.mapper.ViewStatsMapper;
import ru.practicum.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StatServiceImpl implements StatService {
    StatsRepository statsRepository;

    @Autowired
    public StatServiceImpl(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        PageRequest pageable = PageRequest.of(0, 10);

        if (unique.equals(Boolean.TRUE)) {
            return ViewStatsMapper.mapToListDto(statsRepository.getUniqueHits(start, end, uris, pageable));
        } else {
            return ViewStatsMapper.mapToListDto(statsRepository.getHits(start, end, uris, pageable));
        }
    }

    @Override
    @Transactional
    public EndpointHitDto save(EndpointHitDto requestDto) {
        return EndpointHitMapper.mapToDto(statsRepository.save(EndpointHitMapper.mapToEntity(requestDto)));
    }
}
