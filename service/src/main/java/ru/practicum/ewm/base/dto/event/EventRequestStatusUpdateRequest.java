package ru.practicum.ewm.base.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestStatusUpdateRequest {
    @NotNull(message = "When updating, request IDs must exist.")
    List<Long> requestIds;

    @NotBlank(message = "The status of the updated requests must exist and be non-empty.")
    String status;
}
