package co.istad.corefeature.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerTier {
    STANDARD("Standard"),
    SILVER("Silver"),
    GOLD_ATELIER("Gold Atelier"),
    VIP_ROYAL("VIP Royal");

    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
