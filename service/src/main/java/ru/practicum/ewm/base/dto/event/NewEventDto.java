package ru.practicum.ewm.base.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.practicum.ewm.base.dto.location.LocationDto;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @NotBlank(message = "Event short description must exist and not be empty")
    @Length(min = 20, max = 2000)
    String annotation;

    @NotNull
    @Positive(message = "Category ID cannot be a negative number.")
    Long category;

    @NotBlank(message = "Full event description not specified")
    @Length(min = 20, max = 7000)
    String description;

    @NotNull(message = "The event date has not been set")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @Valid
    @NotNull(message = "The coordinates of the event location are not specified.")
    LocationDto location;

    Boolean paid;

    @PositiveOrZero(message = "The limit on the number of participants cannot be a negative number.")
    Long participantLimit;

    Boolean requestModeration;

    @NotBlank(message = "The event title must exist and not be empty.")
    @Length(min = 3, max = 120)
    String title;

    public boolean hasPaid() {
        return this.paid != null;
    }

    public boolean hasParticipantLimit() {
        return this.participantLimit != null;
    }

    public boolean hasRequestModeration() {
        return this.requestModeration != null;
    }
}
