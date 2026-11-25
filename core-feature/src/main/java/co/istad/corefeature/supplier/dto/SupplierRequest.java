package co.istad.corefeature.supplier.dto;

import co.istad.corefeature.common.enums.PaymentTerms;
import co.istad.corefeature.common.enums.SupplierCategory;
import co.istad.corefeature.common.enums.SupplierStatus;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record SupplierRequest(
        @NotBlank(message = "Supplier code is required")
        @Size(max = 60, message = "Supplier code cannot exceed 60 characters")
        String code,

        @NotBlank(message = "Supplier name is required")
        @Size(max = 150, message = "Supplier name cannot exceed 150 characters")
        String name,

        @NotNull(message = "Category is required")
        SupplierCategory category,

        @NotBlank(message = "Contact person is required")
        @Size(max = 100, message = "Contact person cannot exceed 100 characters")
        String contactPerson,

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email format")
        @Size(max = 150, message = "Email cannot exceed 150 characters")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9\\s-]{8,25}$", message = "Phone number must be a valid format (8-25 characters)")
        String phone,

        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address,

        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot exceed 5")
        Integer rating,

        @Positive(message = "Lead time days must be positive")
        Integer leadTimeDays,

        PaymentTerms paymentTerms,

        SupplierStatus status
) {
}
