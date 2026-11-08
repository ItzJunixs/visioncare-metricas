package co.visioncare.vcapi.facturacion.internal.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentData, UUID> {
    List<PaymentData> findAllByInvoiceIdOrderByPaidAtAsc(UUID invoiceId);
}
