package co.visioncare.vcapi.ordenes.internal.service;

import java.util.UUID;

import co.visioncare.vcapi.inventario.InventarioFacade;
import co.visioncare.vcapi.pacientes.PacientesFacade;
import co.visioncare.vcapi.ordenes.internal.entity.PrescriptionSnapshot;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrder;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderNumber;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderRepository;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;

/**
 * HU-08: open a work order for a patient and a frame. Snapshots the patient's current
 * formula through {@link PacientesFacade} (BR-06 note in 06-data/models.md) and rejects
 * when none exists — both ordinary in-process facade calls, not network hops.
 */
public class CreateWorkOrderUseCase {

    private final WorkOrderRepository workOrders;
    private final PacientesFacade pacientes;
    private final InventarioFacade inventario;

    public CreateWorkOrderUseCase(WorkOrderRepository workOrders, PacientesFacade pacientes, InventarioFacade inventario) {
        this.workOrders = workOrders;
        this.pacientes = pacientes;
        this.inventario = inventario;
    }

    public WorkOrder execute(UUID patientId, UUID frameId) {
        if (!pacientes.exists(patientId)) {
            throw new ResourceNotFoundException("Patient not found: " + patientId);
        }
        PacientesFacade.FormulaSnapshot formula = pacientes.currentFormula(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient " + patientId + " has no current optical formula"));
        inventario.findFrame(frameId)
                .orElseThrow(() -> new ResourceNotFoundException("Frame not found: " + frameId));

        PrescriptionSnapshot snapshot = new PrescriptionSnapshot(formula.odSph(), formula.odCyl(), formula.odAxis(),
                formula.oiSph(), formula.oiCyl(), formula.oiAxis(), formula.add(), formula.pupillaryDistance());
        WorkOrder order = WorkOrder.create(WorkOrderNumber.fromSequence(workOrders.nextSequence()),
                patientId, frameId, snapshot);
        return workOrders.save(order);
    }
}
