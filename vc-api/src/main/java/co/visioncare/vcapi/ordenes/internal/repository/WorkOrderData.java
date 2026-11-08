package co.visioncare.vcapi.ordenes.internal.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "work_orders", schema = "ordenes")
public class WorkOrderData {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String number;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "frame_id", nullable = false)
    private UUID frameId;

    @Column(name = "prescription_snapshot", nullable = false, columnDefinition = "text")
    private String prescriptionSnapshot;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "actual_delivery")
    private OffsetDateTime actualDelivery;

    protected WorkOrderData() {
    }

    public WorkOrderData(UUID id, String number, UUID patientId, UUID frameId, String prescriptionSnapshot,
                         String status, OffsetDateTime createdAt, OffsetDateTime actualDelivery) {
        this.id = id;
        this.number = number;
        this.patientId = patientId;
        this.frameId = frameId;
        this.prescriptionSnapshot = prescriptionSnapshot;
        this.status = status;
        this.createdAt = createdAt == null ? OffsetDateTime.now() : createdAt;
        this.actualDelivery = actualDelivery;
    }

    public UUID getId() { return id; }
    public String getNumber() { return number; }
    public UUID getPatientId() { return patientId; }
    public UUID getFrameId() { return frameId; }
    public String getPrescriptionSnapshot() { return prescriptionSnapshot; }
    public String getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getActualDelivery() { return actualDelivery; }
}
