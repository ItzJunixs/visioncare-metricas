package co.visioncare.vcapi.pacientes.internal.service;

import java.util.UUID;

import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormula;
import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormulaRepository;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;

/** Update side of the formula CRUD. BR-02 is re-checked by the entity; BR-03 applies when it becomes current. */
public class UpdateOpticalFormulaUseCase {

    private final OpticalFormulaRepository formulas;

    public UpdateOpticalFormulaUseCase(OpticalFormulaRepository formulas) {
        this.formulas = formulas;
    }

    public OpticalFormula execute(UUID patientId, UUID formulaId, RegisterOpticalFormulaUseCase.Command c) {
        OpticalFormula existing = formulas.findById(formulaId)
                .filter(f -> f.patientId().equals(patientId))
                .orElseThrow(() -> new ResourceNotFoundException("Formula not found: " + formulaId));
        OpticalFormula updated = existing.update(c.odSph(), c.odCyl(), c.odAxis(), c.oiSph(), c.oiCyl(),
                c.oiAxis(), c.add(), c.pupillaryDistance(), c.current(), c.formulaDate());
        if (c.current()) {
            formulas.clearCurrent(patientId); // BR-03
        }
        return formulas.save(updated);
    }
}
