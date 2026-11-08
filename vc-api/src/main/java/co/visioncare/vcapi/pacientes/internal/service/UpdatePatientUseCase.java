package co.visioncare.vcapi.pacientes.internal.service;

import java.util.UUID;

import co.visioncare.vcapi.pacientes.internal.entity.Document;
import co.visioncare.vcapi.pacientes.internal.entity.Patient;
import co.visioncare.vcapi.pacientes.internal.entity.PatientRepository;
import co.visioncare.vcapi.pacientes.internal.entity.exceptions.DuplicateDocumentException;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;

/** Update side of the patient CRUD. BR-01: the document stays unique across patients. */
public class UpdatePatientUseCase {

    private final PatientRepository patients;

    public UpdatePatientUseCase(PatientRepository patients) {
        this.patients = patients;
    }

    public Patient execute(UUID id, RegisterPatientUseCase.Command command) {
        Patient current = patients.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
        String number = command.documentNumber() == null ? "" : command.documentNumber().trim();
        if (patients.existsByDocumentNumberAndIdNot(number, id)) {
            throw new DuplicateDocumentException(number);
        }
        Patient updated = current.update(Document.of(command.documentType(), command.documentNumber()),
                command.firstName(), command.lastName(), command.phone(), command.email(), command.eps());
        return patients.save(updated);
    }
}
