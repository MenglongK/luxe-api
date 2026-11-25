package co.istad.corefeature.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupplierCategory {
    ITALIAN_WOOL("Italian Wool"),
    MONGOLIAN_CASHMERE("Mongolian Cashmere"),
    FRENCH_SILK("French Silk"),
    LEATHER_TANNERIES("Leather Tanneries"),
    PACKAGING_BOXES("Packaging & Boxes");

    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static SupplierCategory fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (SupplierCategory cat : SupplierCategory.values()) {
            if (cat.name().equalsIgnoreCase(value.trim()) || cat.displayName.equalsIgnoreCase(value.trim())) {
                return cat;
            }
        }
        throw new IllegalArgumentException("Unknown supplier category: " + value);
    }
}
