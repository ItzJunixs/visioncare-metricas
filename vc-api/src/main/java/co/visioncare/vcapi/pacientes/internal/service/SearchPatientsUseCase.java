package co.visioncare.vcapi.pacientes.internal.service;

import java.util.List;

import co.visioncare.vcapi.pacientes.internal.entity.Patient;
import co.visioncare.vcapi.pacientes.internal.entity.PatientRepository;

/** HU-03: search patients by name or document; an empty result is normal, never an error. */
public class SearchPatientsUseCase {

    private final PatientRepository patients;

    public SearchPatientsUseCase(PatientRepository patients) {
        this.patients = patients;
    }

    public List<Patient> execute(String query) {
        return patients.search(query);
    }
}
