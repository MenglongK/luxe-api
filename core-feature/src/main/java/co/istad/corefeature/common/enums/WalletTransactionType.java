package co.istad.corefeature.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WalletTransactionType {
    DEPOSIT("Deposit"),
    PAYMENT("Payment"),
    REFUND("Refund"),
    ADJUSTMENT("Adjustment");

    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
