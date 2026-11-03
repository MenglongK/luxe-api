package co.istad.corefeature.facility.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AddressRequest(
        @NotBlank(message = "Address line 1 is required")
        @JsonProperty("addressLine1")
        @JsonAlias({"address_line_1", "addressLine_1", "address_line1"})
        String addressLine1,

        @JsonProperty("addressLine2")
        @JsonAlias({"address_line_2", "addressLine_2", "address_line2"})
        String addressLine2,

        @NotBlank(message = "City is required")
        @JsonProperty("city")
        String city,

        @NotBlank(message = "Province is required")
        @JsonProperty("province")
        String province,

        @NotBlank(message = "Postal code is required")
        @JsonProperty("postalCode")
        @JsonAlias({"postal_code", "postalcode", "zipCode", "zip_code"})
        String postalCode,

        @NotBlank(message = "Country is required")
        @JsonProperty("country")
        String country
) {
}
