package co.visioncare.vcapi.pacientes;

import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the pacientes module — the only entry point other modules may call, per
 * vc-docs/02-domain/module-boundaries.md. Everything else in this module is internal.
 */
public interface PacientesFacade {

    /** Whether the patient has a current optical formula (required before HU-08 creates a work order). */
    boolean hasCurrentFormula(UUID patientId);

    /** The patient's current formula, if any, as an immutable snapshot for a work order. */
    Optional<FormulaSnapshot> currentFormula(UUID patientId);

    boolean exists(UUID patientId);

    record FormulaSnapshot(double odSph, double odCyl, int odAxis, double oiSph, double oiCyl, int oiAxis,
                           Double add, Double pupillaryDistance) {
    }
}
