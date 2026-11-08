package co.visioncare.vcapi.pacientes.internal.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OpticalFormulaRepository {
    OpticalFormula save(OpticalFormula formula);

    Optional<OpticalFormula> findById(UUID id);

    List<OpticalFormula> findAllByPatientId(UUID patientId);

    void deleteById(UUID id);

    Optional<OpticalFormula> findCurrentByPatientId(UUID patientId);

    /** Clears the current flag on the patient's previous formula (BR-03). */
    void clearCurrent(UUID patientId);
}
