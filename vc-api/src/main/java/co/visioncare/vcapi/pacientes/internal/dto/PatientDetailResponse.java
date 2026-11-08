package co.visioncare.vcapi.pacientes.internal.dto;

import java.time.LocalDate;
import java.util.UUID;
import co.visioncare.vcapi.pacientes.internal.service.GetPatientUseCase;
import co.visioncare.vcapi.pacientes.internal.entity.Patient;

public record PatientDetailResponse(UUID id, String documentType, String documentNumber, String firstName,
                                    String lastName, String fullName, String phone, String email, String eps,
                                    String status, FormulaResponse currentFormula, PeriodicControl periodicControl) {
    public static PatientDetailResponse from(GetPatientUseCase.Result r) {
        Patient p = r.patient();
        FormulaResponse formula = r.currentFormula() == null ? null : FormulaResponse.from(r.currentFormula());
        PeriodicControl control = null;
        if (r.currentFormula() != null) {
            LocalDate next = r.currentFormula().formulaDate().plusMonths(12);
            control = new PeriodicControl(next, r.currentFormula().isOverdue() ? "OVERDUE" : "PENDING");
        }
        return new PatientDetailResponse(p.id(), p.document().type().name(), p.document().number(),
                p.firstName(), p.lastName(), p.fullName(), p.phone(), p.email(), p.eps(), p.status().name(),
                formula, control);
    }
}
