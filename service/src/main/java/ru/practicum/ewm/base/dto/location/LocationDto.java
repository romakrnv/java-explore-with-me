package ru.practicum.ewm.base.dto.location;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"lat", "lon"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationDto {
    @NotNull(message = "The latitude must be specified")
    Float lat;

    @NotNull(message = "The longitude must be specified")
    Float lon;
}
