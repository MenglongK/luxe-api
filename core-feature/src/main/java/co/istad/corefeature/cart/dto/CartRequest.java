package co.istad.corefeature.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CartRequest(
        @NotBlank(message = "Customer ID is required")
        String customerId
) {
}
