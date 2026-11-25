package co.istad.corefeature.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupplierStatus {
    ACTIVE("active"),
    REVIEW("review"),
    INACTIVE("inactive");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SupplierStatus fromValue(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }
        for (SupplierStatus status : SupplierStatus.values()) {
            if (status.name().equalsIgnoreCase(val.trim()) || status.value.equalsIgnoreCase(val.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown supplier status: " + val);
    }
}
