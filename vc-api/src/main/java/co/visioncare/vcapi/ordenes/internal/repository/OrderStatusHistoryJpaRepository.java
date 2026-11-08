package co.visioncare.vcapi.ordenes.internal.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryJpaRepository extends JpaRepository<OrderStatusHistoryData, UUID> {
    List<OrderStatusHistoryData> findAllByWorkOrderIdOrderByChangedAtAsc(UUID workOrderId);
}
