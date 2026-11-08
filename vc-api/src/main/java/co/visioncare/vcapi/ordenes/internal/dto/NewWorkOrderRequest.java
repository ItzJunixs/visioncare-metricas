package co.visioncare.vcapi.ordenes.internal.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record NewWorkOrderRequest(@NotNull UUID patientId, @NotNull UUID frameId) {
}
