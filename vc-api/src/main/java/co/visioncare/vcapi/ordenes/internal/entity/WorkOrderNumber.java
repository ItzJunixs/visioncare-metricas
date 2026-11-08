package co.visioncare.vcapi.ordenes.internal.entity;

import java.time.Year;

import co.visioncare.vcapi.shared.exceptions.BusinessRuleException;

public record WorkOrderNumber(String value) {
    public WorkOrderNumber {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("Work order number is required");
        }
        value = value.trim();
    }

    public static WorkOrderNumber fromSequence(long sequence) {
        return new WorkOrderNumber("OT-" + Year.now().getValue() + "-" + String.format("%04d", sequence));
    }

    @Override
    public String toString() {
        return value;
    }
}
