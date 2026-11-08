package co.visioncare.vcapi.inventario.internal.service;

import java.util.UUID;

import co.visioncare.vcapi.inventario.internal.entity.Frame;
import co.visioncare.vcapi.inventario.internal.entity.FrameRepository;
import co.visioncare.vcapi.inventario.internal.entity.Sku;
import co.visioncare.vcapi.inventario.internal.entity.exceptions.DuplicateSkuException;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;
import co.visioncare.vcapi.shared.money.Money;

/** Update side of the frame CRUD. BR-04 (price) and BR-05 (unique SKU) are re-checked; stock is not edited here. */
public class UpdateFrameUseCase {

    private final FrameRepository frames;

    public UpdateFrameUseCase(FrameRepository frames) {
        this.frames = frames;
    }

    public Frame execute(UUID id, RegisterFrameUseCase.Command command) {
        Frame current = frames.findById(id).orElseThrow(() -> new ResourceNotFoundException("Frame not found: " + id));
        Sku sku = new Sku(command.sku());
        if (frames.existsBySkuAndIdNot(sku.value(), id)) {
            throw new DuplicateSkuException(sku.value());
        }
        Frame updated = current.update(sku, command.brand(), command.model(), Money.of(command.buyPrice()),
                Money.of(command.sellPrice()), command.minStock());
        return frames.save(updated);
    }
}
