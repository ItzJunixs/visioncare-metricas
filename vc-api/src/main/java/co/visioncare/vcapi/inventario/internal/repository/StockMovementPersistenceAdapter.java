package co.visioncare.vcapi.inventario.internal.repository;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.visioncare.vcapi.inventario.internal.entity.StockMovementRepository;

@Component
public class StockMovementPersistenceAdapter implements StockMovementRepository {

    private final StockMovementJpaRepository jpa;

    public StockMovementPersistenceAdapter(StockMovementJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean hasExitMovements(UUID frameId) {
        return jpa.existsByFrameIdAndType(frameId, "EXIT");
    }

    @Override
    @Transactional
    public void deleteByFrameId(UUID frameId) {
        jpa.deleteByFrameId(frameId);
    }

    @Override
    public void record(UUID frameId, String type, int quantity, UUID workOrderId) {
        jpa.save(new StockMovementData(UUID.randomUUID(), frameId, type, quantity, workOrderId));
    }
}
