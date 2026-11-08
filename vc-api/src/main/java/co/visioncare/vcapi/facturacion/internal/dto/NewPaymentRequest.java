package co.visioncare.vcapi.facturacion.internal.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record NewPaymentRequest(@NotNull @DecimalMin("0.01") BigDecimal amount, @NotNull String method) {
}
