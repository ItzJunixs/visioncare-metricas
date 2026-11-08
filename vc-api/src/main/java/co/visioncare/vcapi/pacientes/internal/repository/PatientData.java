package co.visioncare.vcapi.pacientes.internal.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "patients", schema = "pacientes")
public class PatientData {

    @Id
    private UUID id;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phone;

    private String email;

    @Column(nullable = false)
    private String eps;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected PatientData() {
    }

    public PatientData(UUID id, String documentType, String documentNumber, String firstName, String lastName,
                       String phone, String email, String eps, String status) {
        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.eps = eps;
        this.status = status;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public String getDocumentType() { return documentType; }
    public String getDocumentNumber() { return documentNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getEps() { return eps; }
    public String getStatus() { return status; }
}
