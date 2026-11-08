package co.visioncare.vcapi.inventario.internal.entity.exceptions;

import co.visioncare.vcapi.shared.exceptions.BusinessRuleException;
import co.visioncare.vcapi.shared.money.Money;

public class InvalidPriceException extends BusinessRuleException {
    public InvalidPriceException(Money sellPrice, Money buyPrice) {
        super("Sell price (" + sellPrice + ") cannot be lower than buy price (" + buyPrice + ")");
    }
}
