package co.istad.corefeature.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Category name is required and cannot be empty")
        @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
        @Pattern(
                regexp = "^[a-zA-Z0-9\\s\\-_&]+$",
                message = "Category name can only contain letters, numbers, spaces, hyphens, underscores, and ampersands (&)"
        )
        String name,

        @PositiveOrZero(message = "Parent ID must be a positive number or zero")
        Long parentId,
        Boolean isActive
) {
}
