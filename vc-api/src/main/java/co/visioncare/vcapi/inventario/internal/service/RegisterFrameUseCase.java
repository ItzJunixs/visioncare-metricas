package co.visioncare.vcapi.inventario.internal.service;

import java.math.BigDecimal;

import co.visioncare.vcapi.inventario.internal.entity.Frame;
import co.visioncare.vcapi.inventario.internal.entity.FrameRepository;
import co.visioncare.vcapi.inventario.internal.entity.Sku;
import co.visioncare.vcapi.inventario.internal.entity.StockMovementRepository;
import co.visioncare.vcapi.inventario.internal.entity.exceptions.DuplicateSkuException;
import co.visioncare.vcapi.shared.money.Money;

/** HU-05, BR-04/BR-05: register a frame, rejecting a duplicate SKU or an invalid price. */
public class RegisterFrameUseCase {

    private final FrameRepository frames;
    private final StockMovementRepository movements;

    public RegisterFrameUseCase(FrameRepository frames, StockMovementRepository movements) {
        this.frames = frames;
        this.movements = movements;
    }

    public Frame execute(Command command) {
        Sku sku = new Sku(command.sku());
        if (frames.existsBySku(sku.value())) {
            throw new DuplicateSkuException(sku.value());
        }
        Frame frame = Frame.register(sku, command.brand(), command.model(), Money.of(command.buyPrice()),
                Money.of(command.sellPrice()), command.stockQuantity(), command.minStock());
        Frame saved = frames.save(frame);
        if (saved.stockQuantity() > 0) {
            movements.record(saved.id(), "ENTRY", saved.stockQuantity(), null); // BR-07
        }
        return saved;
    }

    public record Command(String sku, String brand, String model, BigDecimal buyPrice, BigDecimal sellPrice,
                          int stockQuantity, int minStock) {
    }
}
