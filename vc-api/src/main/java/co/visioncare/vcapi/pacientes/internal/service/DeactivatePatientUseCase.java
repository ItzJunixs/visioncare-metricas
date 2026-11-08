package co.visioncare.vcapi.pacientes.internal.service;

import java.util.UUID;

import co.visioncare.vcapi.pacientes.internal.entity.Patient;
import co.visioncare.vcapi.pacientes.internal.entity.PatientRepository;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;

/** Delete side of the patient CRUD: a logical delete, because work orders reference the patient by id. */
public class DeactivatePatientUseCase {

    private final PatientRepository patients;

    public DeactivatePatientUseCase(PatientRepository patients) {
        this.patients = patients;
    }

    public void execute(UUID id) {
        Patient patient = patients.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
        patients.save(patient.deactivate());
    }
}
