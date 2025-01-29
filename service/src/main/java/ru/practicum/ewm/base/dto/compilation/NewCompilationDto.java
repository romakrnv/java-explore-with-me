package ru.practicum.ewm.base.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewCompilationDto {
    Set<Long> events;

    Boolean pinned;

    @NotBlank(message = "The event collection header cannot be missing or empty")
    @Length(min = 1, max = 50)
    String title;
}
