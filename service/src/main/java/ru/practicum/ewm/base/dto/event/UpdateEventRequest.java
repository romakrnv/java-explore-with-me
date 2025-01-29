package ru.practicum.ewm.base.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.practicum.ewm.base.dto.location.LocationDto;
import ru.practicum.ewm.base.util.DatePattern;
import ru.practicum.ewm.base.util.IfNotNullIsNotEmpty.IfNotNullIsNotEmpty;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class UpdateEventRequest {
    @IfNotNullIsNotEmpty
    @Length(min = 20, max = 2000)
    String annotation;

    @Positive(message = "The category ID cannot be a negative number.")
    Long category;

    @IfNotNullIsNotEmpty
    @Length(min = 20, max = 7000)
    String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DatePattern.DATE_TIME_PATTERN)
    LocalDateTime eventDate;

    @Valid
    LocationDto location;

    Boolean paid;

    @PositiveOrZero(message = "The limit on the number of participants cannot be a negative number.")
    Long participantLimit;

    Boolean requestModeration;

    @IfNotNullIsNotEmpty
    @Length(min = 3, max = 120)
    String title;

    public boolean hasAnnotation() {
        return this.annotation != null;
    }

    public boolean hasCategory() {
        return this.category != null;
    }

    public boolean hasDescription() {
        return this.description != null;
    }

    public boolean hasEventDate() {
        return this.eventDate != null;
    }

    public boolean hasLocation() {
        return this.location != null;
    }

    public boolean hasPaid() {
        return this.paid != null;
    }

    public boolean hasParticipantLimit() {
        return this.participantLimit != null;
    }

    public boolean hasRequestModeration() {
        return this.requestModeration != null;
    }

    public boolean hasTitle() {
        return this.title != null;
    }
}