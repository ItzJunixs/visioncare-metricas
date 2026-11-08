package co.visioncare.vcapi.inventario.internal.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "frames", schema = "inventario")
public class FrameData {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(name = "buy_price", nullable = false)
    private BigDecimal buyPrice;

    @Column(name = "sell_price", nullable = false)
    private BigDecimal sellPrice;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "min_stock", nullable = false)
    private int minStock;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected FrameData() {
    }

    public FrameData(UUID id, String sku, String brand, String model, BigDecimal buyPrice, BigDecimal sellPrice,
                     int stockQuantity, int minStock) {
        this.id = id;
        this.sku = sku;
        this.brand = brand;
        this.model = model;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.stockQuantity = stockQuantity;
        this.minStock = minStock;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public BigDecimal getBuyPrice() { return buyPrice; }
    public BigDecimal getSellPrice() { return sellPrice; }
    public int getStockQuantity() { return stockQuantity; }
    public int getMinStock() { return minStock; }
}
