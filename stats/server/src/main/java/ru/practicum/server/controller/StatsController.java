package ru.practicum.server.controller;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.server.exception.BadRequestException;
import ru.practicum.server.service.StatService;

import java.time.LocalDateTime;
import java.util.List;


@RequiredArgsConstructor
@RestController
public class StatsController {
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final StatService service;

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<ViewStatsDto> get(@RequestParam(required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime start,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime end,
                                  @RequestParam(required = false) List<String> uris,
                                  @RequestParam(defaultValue = "false") Boolean unique) {
        if (start == null || end == null) {
            throw new BadRequestException("The start or end date of the statistics search period is not set.");
        }

        if (start.isAfter(end)) {
            throw new BadRequestException("The start date is later than the end date!");
        }
        return service.get(start, end, uris, unique);
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto save(@RequestBody EndpointHitDto dto) {
        return service.save(dto);
    }
}
