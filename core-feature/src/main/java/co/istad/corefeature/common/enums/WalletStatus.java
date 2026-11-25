package co.istad.corefeature.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WalletStatus {
    ACTIVE("Active"),
    FROZEN("Frozen"),
    CLOSED("Closed");

    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
