package ru.practicum.ewm.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.admin.service.category.AdminCategoryService;
import ru.practicum.ewm.base.dto.category.CategoryDto;
import ru.practicum.ewm.base.dto.category.NewCategoryDto;
import ru.practicum.ewm.base.exceptions.DuplicatedDataException;


@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/categories")
public class AdminCategoriesController {
    private static final String M_CAT_ID = "/{cat-id}";
    private static final String PV_CAT_ID = "cat-id";

    private final AdminCategoryService service;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto save(@RequestBody @Valid NewCategoryDto request) {
        return service.save(request);
    }

    @PatchMapping(M_CAT_ID)
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto update(@PathVariable(PV_CAT_ID) Long categoryId,
                              @RequestBody @Valid NewCategoryDto request) {
        CategoryDto result;
        try {
            result = service.update(request, categoryId);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatedDataException(e.getMessage(), e);
        }

        return result;
    }

    @DeleteMapping(M_CAT_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(PV_CAT_ID) Long categoryId) {
        service.delete(categoryId);
    }
}
