package co.visioncare.vcapi.inventario.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.visioncare.vcapi.inventario.internal.entity.Frame;
import co.visioncare.vcapi.inventario.internal.entity.FrameRepository;

@Component
public class FramePersistenceAdapter implements FrameRepository {

    private final FrameJpaRepository jpa;

    public FramePersistenceAdapter(FrameJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpa.existsBySku(sku);
    }

    @Override
    public boolean existsBySkuAndIdNot(String sku, UUID id) {
        return jpa.existsBySkuAndIdNot(sku, id);
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    public Frame save(Frame frame) {
        return FrameMapper.toDomain(jpa.save(FrameMapper.toData(frame)));
    }

    @Override
    public Optional<Frame> findById(UUID id) {
        return jpa.findById(id).map(FrameMapper::toDomain);
    }

    @Override
    public List<Frame> findAll(boolean lowStockOnly) {
        List<FrameData> rows = lowStockOnly ? jpa.findAllLowStock() : jpa.findAllByOrderByBrandAsc();
        return rows.stream().map(FrameMapper::toDomain).toList();
    }
}
