package co.visioncare.vcapi.inventario.internal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.visioncare.vcapi.inventario.InventarioFacade;
import co.visioncare.vcapi.inventario.internal.entity.Frame;
import co.visioncare.vcapi.inventario.internal.entity.FrameRepository;
import co.visioncare.vcapi.inventario.internal.entity.StockMovementRepository;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;

@Component
public class InventarioFacadeImpl implements InventarioFacade {

    private final FrameRepository frames;
    private final StockMovementRepository movements;

    public InventarioFacadeImpl(FrameRepository frames, StockMovementRepository movements) {
        this.frames = frames;
        this.movements = movements;
    }

    @Override
    public Optional<FrameSnapshot> findFrame(UUID frameId) {
        return frames.findById(frameId).map(InventarioFacadeImpl::toSnapshot);
    }

    @Override
    public void reserveStock(UUID frameId, int quantity, UUID workOrderId) {
        Frame frame = frames.findById(frameId)
                .orElseThrow(() -> new ResourceNotFoundException("Frame not found: " + frameId));
        frame.deductStock(quantity); // BR-06
        frames.save(frame);
        movements.record(frameId, "EXIT", quantity, workOrderId); // BR-07
    }

    private static FrameSnapshot toSnapshot(Frame f) {
        return new FrameSnapshot(f.id(), f.sku().value(), f.brand(), f.model(), f.sellPrice().value(), f.stockQuantity());
    }
}
