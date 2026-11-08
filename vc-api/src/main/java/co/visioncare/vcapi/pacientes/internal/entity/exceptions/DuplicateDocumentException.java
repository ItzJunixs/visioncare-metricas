package co.visioncare.vcapi.pacientes.internal.entity.exceptions;

import co.visioncare.vcapi.shared.exceptions.ConflictException;

/** BR-01: a patient with the same document already exists. */
public class DuplicateDocumentException extends ConflictException {
    public DuplicateDocumentException(String documentNumber) {
        super("A patient with document " + documentNumber + " already exists");
    }
}
