package co.visioncare.vcapi.inventario.internal.dto;

import java.math.BigDecimal;
import java.util.UUID;
import co.visioncare.vcapi.inventario.internal.entity.Frame;

public record FrameResponse(UUID id, String sku, String brand, String model, BigDecimal buyPrice,
                            BigDecimal sellPrice, int stockQuantity, int minStock, boolean belowMinStock) {
    public static FrameResponse from(Frame f) {
        return new FrameResponse(f.id(), f.sku().value(), f.brand(), f.model(), f.buyPrice().value(),
                f.sellPrice().value(), f.stockQuantity(), f.minStock(), f.belowMinStock());
    }
}
