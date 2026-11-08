package co.visioncare.vcapi.inventario.internal.entity;

import java.util.UUID;

import co.visioncare.vcapi.inventario.internal.entity.exceptions.InsufficientStockException;
import co.visioncare.vcapi.inventario.internal.entity.exceptions.InvalidPriceException;
import co.visioncare.vcapi.shared.exceptions.BusinessRuleException;
import co.visioncare.vcapi.shared.money.Money;

/** Frame aggregate root (HU-05). BR-04: sellPrice >= buyPrice. BR-06: stock never negative. */
public class Frame {

    private final UUID id;
    private final Sku sku;
    private final String brand;
    private final String model;
    private final Money buyPrice;
    private final Money sellPrice;
    private int stockQuantity;
    private int minStock;

    private Frame(UUID id, Sku sku, String brand, String model, Money buyPrice, Money sellPrice,
                  int stockQuantity, int minStock) {
        this.id = id;
        this.sku = sku;
        this.brand = brand;
        this.model = model;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.stockQuantity = stockQuantity;
        this.minStock = minStock;
    }

    public static Frame register(Sku sku, String brand, String model, Money buyPrice, Money sellPrice,
                                 int stockQuantity, int minStock) {
        if (stockQuantity < 0) {
            throw new BusinessRuleException("Stock quantity cannot be negative");
        }
        if (minStock < 0) {
            throw new BusinessRuleException("Minimum stock cannot be negative");
        }
        if (sellPrice.isLessThan(buyPrice)) {
            throw new InvalidPriceException(sellPrice, buyPrice);
        }
        return new Frame(UUID.randomUUID(), sku, required(brand, "brand"), required(model, "model"),
                buyPrice, sellPrice, stockQuantity, minStock);
    }

    public static Frame rehydrate(UUID id, Sku sku, String brand, String model, Money buyPrice, Money sellPrice,
                                  int stockQuantity, int minStock) {
        return new Frame(id, sku, brand, model, buyPrice, sellPrice, stockQuantity, minStock);
    }

    /** Replaces the editable data. The stock quantity is untouched: it only moves through stock movements (BR-07). */
    public Frame update(Sku newSku, String newBrand, String newModel, Money newBuyPrice, Money newSellPrice,
                        int newMinStock) {
        if (newMinStock < 0) {
            throw new BusinessRuleException("Minimum stock cannot be negative");
        }
        if (newSellPrice.isLessThan(newBuyPrice)) {
            throw new InvalidPriceException(newSellPrice, newBuyPrice);
        }
        return new Frame(id, newSku, required(newBrand, "brand"), required(newModel, "model"),
                newBuyPrice, newSellPrice, stockQuantity, newMinStock);
    }

    /** BR-06/BR-09: decrements stock, rejecting when it would go negative. */
    public void deductStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleException("Quantity to deduct must be positive");
        }
        if (stockQuantity < quantity) {
            throw new InsufficientStockException(sku.value());
        }
        this.stockQuantity -= quantity;
    }

    public void setMinStock(int minStock) {
        if (minStock < 0) {
            throw new BusinessRuleException("Minimum stock cannot be negative");
        }
        this.minStock = minStock;
    }

    public boolean belowMinStock() {
        return stockQuantity <= minStock;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("The field '" + field + "' is required");
        }
        return value.trim();
    }

    public UUID id() { return id; }
    public Sku sku() { return sku; }
    public String brand() { return brand; }
    public String model() { return model; }
    public Money buyPrice() { return buyPrice; }
    public Money sellPrice() { return sellPrice; }
    public int stockQuantity() { return stockQuantity; }
    public int minStock() { return minStock; }
}
