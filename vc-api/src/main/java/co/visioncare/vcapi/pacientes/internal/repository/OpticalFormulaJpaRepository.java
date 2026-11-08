package co.visioncare.vcapi.pacientes.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OpticalFormulaJpaRepository extends JpaRepository<OpticalFormulaData, UUID> {
    Optional<OpticalFormulaData> findByPatientIdAndCurrentTrue(UUID patientId);

    List<OpticalFormulaData> findAllByPatientIdOrderByFormulaDateDesc(UUID patientId);

    List<OpticalFormulaData> findAllByPatientIdAndCurrentTrue(UUID patientId);
}
