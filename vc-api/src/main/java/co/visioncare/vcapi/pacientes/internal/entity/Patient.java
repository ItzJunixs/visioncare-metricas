package co.visioncare.vcapi.pacientes.internal.entity;

import co.visioncare.vcapi.shared.exceptions.BusinessRuleException;
import java.util.UUID;

/** Patient aggregate root (HU-01). BR-01: document type+number unique. */
public class Patient {

    private final UUID id;
    private final Document document;
    private final String firstName;
    private final String lastName;
    private final String phone;
    private final String email;
    private final String eps;
    private final PatientStatus status;

    private Patient(UUID id, Document document, String firstName, String lastName, String phone,
                    String email, String eps, PatientStatus status) {
        this.id = id;
        this.document = document;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.eps = eps;
        this.status = status;
    }

    public static Patient register(Document document, String firstName, String lastName, String phone,
                                   String email, String eps) {
        return new Patient(UUID.randomUUID(), document, required(firstName, "first name"),
                required(lastName, "last name"), required(phone, "phone"), blankToNull(email),
                required(eps, "EPS"), PatientStatus.ACTIVE);
    }

    public static Patient rehydrate(UUID id, Document document, String firstName, String lastName, String phone,
                                    String email, String eps, PatientStatus status) {
        return new Patient(id, document, firstName, lastName, phone, email, eps, status);
    }

    /** Replaces the editable data, keeping the identity and the status (BR-01 is checked by the use case). */
    public Patient update(Document newDocument, String newFirstName, String newLastName, String newPhone,
                          String newEmail, String newEps) {
        return new Patient(id, newDocument, required(newFirstName, "first name"), required(newLastName, "last name"),
                required(newPhone, "phone"), blankToNull(newEmail), required(newEps, "EPS"), status);
    }

    /** Logical delete: the row stays because work orders in another module reference it by id. */
    public Patient deactivate() {
        return new Patient(id, document, firstName, lastName, phone, email, eps, PatientStatus.INACTIVE);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("The field '" + field + "' is required");
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    public UUID id() {
        return id;
    }

    public Document document() {
        return document;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public String phone() {
        return phone;
    }

    public String email() {
        return email;
    }

    public String eps() {
        return eps;
    }

    public PatientStatus status() {
        return status;
    }
}
