package co.visioncare.vcapi.pacientes.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormula;
import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormulaRepository;

@Component
public class OpticalFormulaPersistenceAdapter implements OpticalFormulaRepository {

    private final OpticalFormulaJpaRepository jpa;

    public OpticalFormulaPersistenceAdapter(OpticalFormulaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public OpticalFormula save(OpticalFormula formula) {
        OpticalFormulaData saved = jpa.save(new OpticalFormulaData(formula.id(), formula.patientId(),
                formula.odSph(), formula.odCyl(), formula.odAxis(), formula.oiSph(), formula.oiCyl(),
                formula.oiAxis(), formula.add(), formula.pupillaryDistance(), formula.current(),
                formula.formulaDate()));
        return toDomain(saved);
    }

    @Override
    public Optional<OpticalFormula> findById(UUID id) {
        return jpa.findById(id).map(OpticalFormulaPersistenceAdapter::toDomain);
    }

    @Override
    public List<OpticalFormula> findAllByPatientId(UUID patientId) {
        return jpa.findAllByPatientIdOrderByFormulaDateDesc(patientId).stream()
                .map(OpticalFormulaPersistenceAdapter::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    public Optional<OpticalFormula> findCurrentByPatientId(UUID patientId) {
        return jpa.findByPatientIdAndCurrentTrue(patientId).map(OpticalFormulaPersistenceAdapter::toDomain);
    }

    @Override
    @Transactional
    public void clearCurrent(UUID patientId) {
        jpa.findAllByPatientIdAndCurrentTrue(patientId).forEach(d -> {
            d.setCurrent(false);
            jpa.save(d);
        });
    }

    private static OpticalFormula toDomain(OpticalFormulaData d) {
        return OpticalFormula.rehydrate(d.getId(), d.getPatientId(), d.getOdSph(), d.getOdCyl(), d.getOdAxis(),
                d.getOiSph(), d.getOiCyl(), d.getOiAxis(), d.getAdd(), d.getPupillaryDistance(), d.isCurrent(),
                d.getFormulaDate());
    }
}
