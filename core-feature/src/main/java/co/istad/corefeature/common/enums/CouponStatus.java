package co.istad.corefeature.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponStatus {
    ACTIVE("Active"),
    SCHEDULED("Scheduled"),
    EXPIRED("Expired"),
    DISABLED("Disabled");

    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
