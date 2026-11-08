package co.visioncare.vcapi.pacientes.internal.service;

import java.util.Optional;
import java.util.UUID;

import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;
import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormula;
import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormulaRepository;
import co.visioncare.vcapi.pacientes.internal.entity.Patient;
import co.visioncare.vcapi.pacientes.internal.entity.PatientRepository;

/** HU-02 read side, HU-04: fetch a patient with its current formula and overdue flag. */
public class GetPatientUseCase {

    private final PatientRepository patients;
    private final OpticalFormulaRepository formulas;

    public GetPatientUseCase(PatientRepository patients, OpticalFormulaRepository formulas) {
        this.patients = patients;
        this.formulas = formulas;
    }

    public Result execute(UUID id) {
        Patient patient = patients.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
        Optional<OpticalFormula> formula = formulas.findCurrentByPatientId(id);
        return new Result(patient, formula.orElse(null));
    }

    public record Result(Patient patient, OpticalFormula currentFormula) {
    }
}
