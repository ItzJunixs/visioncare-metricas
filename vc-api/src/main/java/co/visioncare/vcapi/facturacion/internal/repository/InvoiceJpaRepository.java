package co.visioncare.vcapi.facturacion.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceData, UUID> {

    Optional<InvoiceData> findByWorkOrderId(UUID workOrderId);

    List<InvoiceData> findAllByOrderByCreatedAtDesc();

    @Query(value = "select nextval('facturacion.invoice_number_seq')", nativeQuery = true)
    long nextSequenceValue();
}
