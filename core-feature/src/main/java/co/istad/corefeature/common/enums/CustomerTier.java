package co.istad.corefeature.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerTier {
    STANDARD("Standard", 0),
    SILVER("Silver", 1_000),
    GOLD_ATELIER("Gold Atelier", 3_000),
    VIP_ROYAL("VIP Royal", 7_000);

    private final String displayName;
    private final int minPoints;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolves the highest membership tier unlocked by the given lifetime points.
     */
    public static CustomerTier fromPoints(int points) {
        CustomerTier resolved = STANDARD;
        for (CustomerTier tier : values()) {
            if (points >= tier.minPoints) {
                resolved = tier;
            }
        }
        return resolved;
    }

    /**
     * Returns the higher of two tiers so membership upgrades never accidentally downgrade.
     */
    public static CustomerTier higherOf(CustomerTier current, CustomerTier candidate) {
        if (current == null) {
            return candidate != null ? candidate : STANDARD;
        }
        if (candidate == null) {
            return current;
        }
        return candidate.ordinal() >= current.ordinal() ? candidate : current;
    }

    public CustomerTier nextTier() {
        CustomerTier[] tiers = values();
        int nextIndex = ordinal() + 1;
        return nextIndex < tiers.length ? tiers[nextIndex] : null;
    }

    public int pointsToNextTier(int lifetimePoints) {
        CustomerTier next = nextTier();
        if (next == null) {
            return 0;
        }
        return Math.max(0, next.minPoints - Math.max(0, lifetimePoints));
    }
}
