package co.visioncare.vcapi.pacientes.internal.entity.exceptions;

import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;

/** The patient has no current optical formula — required before a work order can be created. */
public class NoCurrentFormulaException extends ResourceNotFoundException {
    public NoCurrentFormulaException(java.util.UUID patientId) {
        super("Patient " + patientId + " has no current optical formula");
    }
}
