package co.visioncare.vcapi.inventario.internal.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FrameJpaRepository extends JpaRepository<FrameData, UUID> {
    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, UUID id);

    List<FrameData> findAllByOrderByBrandAsc();

    @Query("select f from FrameData f where f.stockQuantity <= f.minStock order by f.brand")
    List<FrameData> findAllLowStock();
}
