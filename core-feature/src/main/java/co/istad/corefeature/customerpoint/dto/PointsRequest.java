package co.istad.corefeature.customerpoint.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record PointsRequest(
        @NotNull(message = "Points are required")
        @Positive(message = "Points must be greater than zero")
        Integer points,

        String referenceId,

        String description
) {
    public PointsRequest(Integer points) {
        this(points, null, null);
    }
}
