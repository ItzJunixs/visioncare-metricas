package co.visioncare.vcapi.inventario.internal.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FrameRepository {
    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, UUID id);

    void deleteById(UUID id);

    Frame save(Frame frame);

    Optional<Frame> findById(UUID id);

    List<Frame> findAll(boolean lowStockOnly);
}
