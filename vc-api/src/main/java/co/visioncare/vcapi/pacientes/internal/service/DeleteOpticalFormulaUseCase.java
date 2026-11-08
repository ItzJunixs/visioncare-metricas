package co.visioncare.vcapi.pacientes.internal.service;

import java.util.UUID;

import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormulaRepository;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;

/** Delete side of the formula CRUD. Work orders keep their own prescription snapshot, so they are unaffected. */
public class DeleteOpticalFormulaUseCase {

    private final OpticalFormulaRepository formulas;

    public DeleteOpticalFormulaUseCase(OpticalFormulaRepository formulas) {
        this.formulas = formulas;
    }

    public void execute(UUID patientId, UUID formulaId) {
        formulas.findById(formulaId)
                .filter(f -> f.patientId().equals(patientId))
                .orElseThrow(() -> new ResourceNotFoundException("Formula not found: " + formulaId));
        formulas.deleteById(formulaId);
    }
}
