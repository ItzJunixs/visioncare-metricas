package co.visioncare.vcapi.facturacion.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.visioncare.vcapi.facturacion.internal.service.CreateInvoiceUseCase;
import co.visioncare.vcapi.ordenes.WorkOrderApprovedEvent;

/**
 * facturacion reacting to ordenes through an in-process event (ADR-003). AFTER_COMMIT means this
 * only runs once the approval transaction has committed. That transaction is finished, so a
 * handler that simply joined it would write nothing: the invoice is therefore saved in its own
 * transaction (REQUIRES_NEW).
 */
@Component
public class WorkOrderApprovedListener {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderApprovedListener.class);

    private final CreateInvoiceUseCase createInvoice;

    public WorkOrderApprovedListener(CreateInvoiceUseCase createInvoice) {
        this.createInvoice = createInvoice;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onWorkOrderApproved(WorkOrderApprovedEvent event) {
        try {
            createInvoice.execute(event.workOrderId(), event.frameSellPrice());
            log.info("Invoice issued for work order {}", event.orderCode());
        } catch (RuntimeException ex) {
            log.error("Could not issue the invoice for work order {}", event.orderCode(), ex);
            throw ex;
        }
    }
}
