package co.visioncare.vcapi.inventario.internal.entity;

import co.visioncare.vcapi.shared.exceptions.BusinessRuleException;

public record Sku(String value) {
    public Sku {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("SKU is required");
        }
        value = value.trim().toUpperCase();
    }

    @Override
    public String toString() {
        return value;
    }
}
