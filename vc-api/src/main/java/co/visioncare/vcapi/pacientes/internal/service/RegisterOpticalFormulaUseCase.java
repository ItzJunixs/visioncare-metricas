package co.visioncare.vcapi.pacientes.internal.service;

import java.time.LocalDate;
import java.util.UUID;

import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;
import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormula;
import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormulaRepository;
import co.visioncare.vcapi.pacientes.internal.entity.PatientRepository;

/** HU-02, BR-02/BR-03: register a new formula; marking it current supersedes the previous one. */
public class RegisterOpticalFormulaUseCase {

    private final OpticalFormulaRepository formulas;
    private final PatientRepository patients;

    public RegisterOpticalFormulaUseCase(OpticalFormulaRepository formulas, PatientRepository patients) {
        this.formulas = formulas;
        this.patients = patients;
    }

    public OpticalFormula execute(UUID patientId, Command command) {
        patients.findById(patientId).orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));
        if (command.current()) {
            formulas.clearCurrent(patientId); // BR-03: a state transition, not a rejection
        }
        OpticalFormula formula = OpticalFormula.register(patientId, command.odSph(), command.odCyl(),
                command.odAxis(), command.oiSph(), command.oiCyl(), command.oiAxis(), command.add(),
                command.pupillaryDistance(), command.current(), command.formulaDate());
        return formulas.save(formula);
    }

    public record Command(double odSph, double odCyl, int odAxis, double oiSph, double oiCyl, int oiAxis,
                          Double add, Double pupillaryDistance, boolean current, LocalDate formulaDate) {
    }
}
