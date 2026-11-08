package co.visioncare.vcapi.pacientes.internal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.visioncare.vcapi.pacientes.PacientesFacade;
import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormula;
import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormulaRepository;
import co.visioncare.vcapi.pacientes.internal.entity.PatientRepository;

@Component
public class PacientesFacadeImpl implements PacientesFacade {

    private final PatientRepository patients;
    private final OpticalFormulaRepository formulas;

    public PacientesFacadeImpl(PatientRepository patients, OpticalFormulaRepository formulas) {
        this.patients = patients;
        this.formulas = formulas;
    }

    @Override
    public boolean hasCurrentFormula(UUID patientId) {
        return formulas.findCurrentByPatientId(patientId).isPresent();
    }

    @Override
    public Optional<FormulaSnapshot> currentFormula(UUID patientId) {
        return formulas.findCurrentByPatientId(patientId).map(PacientesFacadeImpl::toSnapshot);
    }

    @Override
    public boolean exists(UUID patientId) {
        return patients.findById(patientId).isPresent();
    }

    private static FormulaSnapshot toSnapshot(OpticalFormula f) {
        return new FormulaSnapshot(f.odSph(), f.odCyl(), f.odAxis(), f.oiSph(), f.oiCyl(), f.oiAxis(),
                f.add(), f.pupillaryDistance());
    }
}
