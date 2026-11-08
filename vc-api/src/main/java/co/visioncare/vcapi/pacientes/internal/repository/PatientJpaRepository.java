package co.visioncare.vcapi.pacientes.internal.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientJpaRepository extends JpaRepository<PatientData, UUID> {

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id);

    List<PatientData> findAllByOrderByCreatedAtDesc();

    @Query("""
            select p from PatientData p
            where lower(p.firstName) like lower(concat('%', :q, '%'))
               or lower(p.lastName) like lower(concat('%', :q, '%'))
               or p.documentNumber like concat('%', :q, '%')
            order by p.createdAt desc
            """)
    List<PatientData> search(@Param("q") String q);
}
