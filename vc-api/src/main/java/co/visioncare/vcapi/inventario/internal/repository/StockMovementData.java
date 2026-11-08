package co.visioncare.vcapi.inventario.internal.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_movements", schema = "inventario")
public class StockMovementData {

    @Id
    private UUID id;

    @Column(name = "frame_id", nullable = false)
    private UUID frameId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "work_order_id")
    private UUID workOrderId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected StockMovementData() {
    }

    public StockMovementData(UUID id, UUID frameId, String type, int quantity, UUID workOrderId) {
        this.id = id;
        this.frameId = frameId;
        this.type = type;
        this.quantity = quantity;
        this.workOrderId = workOrderId;
        this.createdAt = OffsetDateTime.now();
    }
}
