package co.visioncare.vcapi.inventario.internal.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record NewFrameRequest(@NotBlank String sku, @NotBlank String brand, @NotBlank String model,
                              @NotNull @PositiveOrZero BigDecimal buyPrice,
                              @NotNull @PositiveOrZero BigDecimal sellPrice,
                              @PositiveOrZero int stockQuantity, @PositiveOrZero Integer minStock) {
}
