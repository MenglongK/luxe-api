package co.istad.corefeature.brand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record BrandRequest(
        @NotBlank(message = "Name cannot be blank")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        @Pattern(
                regexp = "^[a-zA-Z0-9\\s\\-_&]+$",
                message = "Name contains invalid characters. Only alphanumeric, spaces, hyphens, underscores, and ampersands are allowed."
        )
        String name,

        @NotBlank(message = "Logo URL cannot be blank")
        @Size(max = 2048, message = "URL is too long")
//        @URL(protocol = "https", message = "Must be a valid HTTPS URL")
//        @Pattern(
//                regexp = "^https://.*\\.(png|jpg|jpeg|svg|gif)$",
//                message = "URL must point to a valid image file extension (png, jpg, jpeg, svg, gif)"
//        )
        String logoUrl,

        Boolean isActive
) {
}
