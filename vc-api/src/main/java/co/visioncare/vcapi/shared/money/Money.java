package co.visioncare.vcapi.shared.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import co.visioncare.vcapi.shared.exceptions.BusinessRuleException;

/** Value object for monetary amounts. Immutable, never negative, scale of 2. */
public final class Money {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        if (amount == null) {
            throw new BusinessRuleException("Amount is required");
        }
        if (amount.signum() < 0) {
            throw new BusinessRuleException("Amount cannot be negative");
        }
        return new Money(amount);
    }

    public static Money of(double amount) {
        return of(BigDecimal.valueOf(amount));
    }

    public BigDecimal value() {
        return amount;
    }

    public boolean isLessThan(Money other) {
        return amount.compareTo(other.amount) < 0;
    }

    public boolean isGreaterThan(Money other) {
        return amount.compareTo(other.amount) > 0;
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money minus(Money other) {
        BigDecimal r = amount.subtract(other.amount);
        return new Money(r.signum() < 0 ? BigDecimal.ZERO : r);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money m && amount.compareTo(m.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
