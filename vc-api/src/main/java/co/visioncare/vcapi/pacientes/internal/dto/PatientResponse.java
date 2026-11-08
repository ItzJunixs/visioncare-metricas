package co.visioncare.vcapi.pacientes.internal.dto;

import java.util.UUID;
import co.visioncare.vcapi.pacientes.internal.entity.Patient;

public record PatientResponse(UUID id, String documentType, String documentNumber, String firstName,
                              String lastName, String fullName, String phone, String email, String eps,
                              String status) {
    public static PatientResponse from(Patient p) {
        return new PatientResponse(p.id(), p.document().type().name(), p.document().number(), p.firstName(),
                p.lastName(), p.fullName(), p.phone(), p.email(), p.eps(), p.status().name());
    }
}
