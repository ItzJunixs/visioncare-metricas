package co.visioncare.vcapi.ordenes.internal.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WorkOrderJpaRepository extends JpaRepository<WorkOrderData, UUID> {

    List<WorkOrderData> findAllByOrderByCreatedAtDesc();

    List<WorkOrderData> findAllByStatusOrderByCreatedAtDesc(String status);

    @Query(value = "select nextval('ordenes.work_order_number_seq')", nativeQuery = true)
    long nextSequenceValue();
}
