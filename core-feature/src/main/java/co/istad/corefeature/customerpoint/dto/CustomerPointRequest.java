package co.istad.corefeature.customerpoint.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CustomerPointRequest(
        @NotBlank(message = "Customer ID is required")
        String customerId,

        Integer totalPoints
) {
}
