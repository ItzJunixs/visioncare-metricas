package co.visioncare.vcapi.pacientes.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.visioncare.vcapi.pacientes.internal.entity.Patient;
import co.visioncare.vcapi.pacientes.internal.entity.PatientRepository;

@Component
public class PatientPersistenceAdapter implements PatientRepository {

    private final PatientJpaRepository jpa;

    public PatientPersistenceAdapter(PatientJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return jpa.existsByDocumentNumber(documentNumber);
    }

    @Override
    public boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id) {
        return jpa.existsByDocumentNumberAndIdNot(documentNumber, id);
    }

    @Override
    public Patient save(Patient patient) {
        return PatientMapper.toDomain(jpa.save(PatientMapper.toData(patient)));
    }

    @Override
    public Optional<Patient> findById(UUID id) {
        return jpa.findById(id).map(PatientMapper::toDomain);
    }

    @Override
    public List<Patient> search(String query) {
        List<PatientData> rows = (query == null || query.isBlank())
                ? jpa.findAllByOrderByCreatedAtDesc()
                : jpa.search(query.trim());
        return rows.stream().map(PatientMapper::toDomain).toList();
    }
}
