package co.istad.corefeature.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointTransactionType {
    EARNED("Earned"),
    REDEEMED("Redeemed"),
    ADJUSTED("Adjusted"),
    EXPIRED("Expired"),
    REFUNDED("Refunded");

    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
