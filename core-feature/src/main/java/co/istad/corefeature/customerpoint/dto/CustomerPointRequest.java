package co.istad.corefeature.customerpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

@Builder
public record CustomerPointRequest(
        @NotBlank(message = "Customer ID is required")
        String customerId,

        @PositiveOrZero(message = "Total points cannot be negative")
        Integer totalPoints
) {
}
