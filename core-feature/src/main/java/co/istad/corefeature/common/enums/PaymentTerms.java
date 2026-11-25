package co.istad.corefeature.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentTerms {
    NET_30("Net 30"),
    NET_60("Net 60"),
    IMMEDIATE("Immediate"),
    LETTER_OF_CREDIT("Letter of Credit");

    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static PaymentTerms fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (PaymentTerms terms : PaymentTerms.values()) {
            if (terms.name().equalsIgnoreCase(value.trim())
                    || terms.displayName.equalsIgnoreCase(value.trim())
                    || terms.name().replace("_", "").equalsIgnoreCase(value.replace(" ", "").trim())) {
                return terms;
            }
        }
        throw new IllegalArgumentException("Unknown payment terms: " + value);
    }
}
