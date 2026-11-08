package co.visioncare.vcapi.ordenes.internal.repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.visioncare.vcapi.ordenes.internal.entity.PrescriptionSnapshot;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrder;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderNumber;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderStatus;

final class WorkOrderMapper {

    private static final ObjectMapper JSON = new ObjectMapper();

    private WorkOrderMapper() {
    }

    static WorkOrder toDomain(WorkOrderData d) {
        try {
            PrescriptionSnapshot snapshot = JSON.readValue(d.getPrescriptionSnapshot(), PrescriptionSnapshot.class);
            return WorkOrder.rehydrate(d.getId(), new WorkOrderNumber(d.getNumber()), d.getPatientId(),
                    d.getFrameId(), snapshot, WorkOrderStatus.valueOf(d.getStatus()), d.getCreatedAt(),
                    d.getActualDelivery());
        } catch (Exception e) {
            throw new IllegalStateException("Corrupt prescription snapshot for work order " + d.getId(), e);
        }
    }

    static WorkOrderData toData(WorkOrder o) {
        try {
            String snapshot = JSON.writeValueAsString(o.prescriptionSnapshot());
            return new WorkOrderData(o.id(), o.number().value(), o.patientId(), o.frameId(), snapshot,
                    o.status().name(), o.createdAt(), o.actualDelivery());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize prescription snapshot", e);
        }
    }
}
