package co.visioncare.vcapi.pacientes.internal.service;

import java.util.List;
import java.util.UUID;

import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormula;
import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormulaRepository;
import co.visioncare.vcapi.pacientes.internal.entity.PatientRepository;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;

/** Read side of the optical formula CRUD: a patient's formulas, newest first. */
public class ListOpticalFormulasUseCase {

    private final PatientRepository patients;
    private final OpticalFormulaRepository formulas;

    public ListOpticalFormulasUseCase(PatientRepository patients, OpticalFormulaRepository formulas) {
        this.patients = patients;
        this.formulas = formulas;
    }

    public List<OpticalFormula> execute(UUID patientId) {
        patients.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));
        return formulas.findAllByPatientId(patientId);
    }
}
