package co.visioncare.vcapi.pacientes.internal.entity;

import co.visioncare.vcapi.shared.exceptions.BusinessRuleException;

/** Value object: a patient's identity document (type + number). BR-01: unique together. */
public record Document(DocumentType type, String number) {

    public Document {
        if (type == null) {
            throw new BusinessRuleException("Document type is required");
        }
        if (number == null || number.isBlank()) {
            throw new BusinessRuleException("Document number is required");
        }
        number = number.trim();
    }

    public static Document of(String type, String number) {
        DocumentType parsed;
        try {
            parsed = DocumentType.valueOf(type == null ? "" : type.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Unsupported document type: " + type);
        }
        return new Document(parsed, number);
    }
}
