package co.visioncare.vcapi.pacientes.internal.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository {
    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id);

    Patient save(Patient patient);

    Optional<Patient> findById(UUID id);

    List<Patient> search(String query);
}
