package ru.practicum.ewm.admin.service.category;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.base.dto.category.CategoryDto;
import ru.practicum.ewm.base.dto.category.NewCategoryDto;
import ru.practicum.ewm.base.exceptions.ConditionsNotMetException;
import ru.practicum.ewm.base.exceptions.ConflictException;
import ru.practicum.ewm.base.exceptions.NotFoundException;
import ru.practicum.ewm.base.mapper.CategoryMapper;
import ru.practicum.ewm.base.models.Category;
import ru.practicum.ewm.base.repository.category.CategoryRepository;
import ru.practicum.ewm.base.repository.event.EventRepository;


@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {

    CategoryRepository categoryRepository;
    EventRepository eventRepository;

    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Category c ID %d not found", categoryId)));
    }

    @Override
    @Transactional
    public CategoryDto save(NewCategoryDto request) {
        Category category = CategoryMapper.mapToEntity(request);

        if (categoryRepository.existsByName(request.getName())) {
            throw new ConflictException(String.format("category \"%s\" already exists", category.getName()));
        }

        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(e.getMessage(), e);
        }

        return CategoryMapper.mapToDto(category);
    }

    @Override
    @Transactional
    public CategoryDto update(NewCategoryDto request, Long catId) {
        Category updatedCategory = CategoryMapper.updateFields(findById(catId), request);

        updatedCategory = categoryRepository.save(updatedCategory);

        return CategoryMapper.mapToDto(updatedCategory);
    }

    @Override
    @Transactional
    public void delete(Long categoryId) {
        Category category = findById(categoryId);
        if (eventRepository.existsByCategory(category)) {
            throw new ConditionsNotMetException(String.format("There are events related to the %s category, " +
                    "deleting a category is not possible.", category.getName()));
        } else {
            categoryRepository.deleteById(categoryId);
        }
    }
}
