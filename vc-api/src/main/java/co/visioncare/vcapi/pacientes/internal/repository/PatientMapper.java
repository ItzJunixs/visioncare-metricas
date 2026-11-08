package co.visioncare.vcapi.pacientes.internal.repository;

import co.visioncare.vcapi.pacientes.internal.entity.Document;
import co.visioncare.vcapi.pacientes.internal.entity.DocumentType;
import co.visioncare.vcapi.pacientes.internal.entity.Patient;
import co.visioncare.vcapi.pacientes.internal.entity.PatientStatus;

final class PatientMapper {

    private PatientMapper() {
    }

    static Patient toDomain(PatientData d) {
        return Patient.rehydrate(d.getId(), new Document(DocumentType.valueOf(d.getDocumentType()), d.getDocumentNumber()),
                d.getFirstName(), d.getLastName(), d.getPhone(), d.getEmail(), d.getEps(),
                PatientStatus.valueOf(d.getStatus()));
    }

    static PatientData toData(Patient p) {
        return new PatientData(p.id(), p.document().type().name(), p.document().number(), p.firstName(),
                p.lastName(), p.phone(), p.email(), p.eps(), p.status().name());
    }
}
