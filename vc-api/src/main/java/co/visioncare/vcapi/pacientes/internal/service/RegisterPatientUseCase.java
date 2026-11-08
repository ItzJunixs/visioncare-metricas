package co.visioncare.vcapi.pacientes.internal.service;

import co.visioncare.vcapi.pacientes.internal.entity.Document;
import co.visioncare.vcapi.pacientes.internal.entity.Patient;
import co.visioncare.vcapi.pacientes.internal.entity.PatientRepository;
import co.visioncare.vcapi.pacientes.internal.entity.exceptions.DuplicateDocumentException;

/** HU-01, BR-01: register a patient, rejecting a duplicate document. */
public class RegisterPatientUseCase {

    private final PatientRepository patients;

    public RegisterPatientUseCase(PatientRepository patients) {
        this.patients = patients;
    }

    public Patient execute(Command command) {
        String number = command.documentNumber() == null ? "" : command.documentNumber().trim();
        if (patients.existsByDocumentNumber(number)) {
            throw new DuplicateDocumentException(number);
        }
        Patient patient = Patient.register(
                Document.of(command.documentType(), command.documentNumber()),
                command.firstName(), command.lastName(), command.phone(), command.email(), command.eps());
        return patients.save(patient);
    }

    public record Command(String documentType, String documentNumber, String firstName, String lastName,
                          String phone, String email, String eps) {
    }
}
