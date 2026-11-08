package co.visioncare.vcapi.inventario.internal.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementJpaRepository extends JpaRepository<StockMovementData, UUID> {

    boolean existsByFrameIdAndType(UUID frameId, String type);

    void deleteByFrameId(UUID frameId);
}
