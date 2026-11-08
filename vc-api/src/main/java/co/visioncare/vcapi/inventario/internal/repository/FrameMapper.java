package co.visioncare.vcapi.inventario.internal.repository;

import co.visioncare.vcapi.inventario.internal.entity.Frame;
import co.visioncare.vcapi.inventario.internal.entity.Sku;
import co.visioncare.vcapi.shared.money.Money;

final class FrameMapper {

    private FrameMapper() {
    }

    static Frame toDomain(FrameData d) {
        return Frame.rehydrate(d.getId(), new Sku(d.getSku()), d.getBrand(), d.getModel(),
                Money.of(d.getBuyPrice()), Money.of(d.getSellPrice()), d.getStockQuantity(), d.getMinStock());
    }

    static FrameData toData(Frame f) {
        return new FrameData(f.id(), f.sku().value(), f.brand(), f.model(), f.buyPrice().value(),
                f.sellPrice().value(), f.stockQuantity(), f.minStock());
    }
}
