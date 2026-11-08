package co.visioncare.vcapi.pacientes.internal.dto;

import java.time.LocalDate;
import java.util.UUID;
import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormula;

public record FormulaResponse(UUID id, double odSph, double odCyl, int odAxis, double oiSph, double oiCyl,
                              int oiAxis, Double add, Double pupillaryDistance, boolean isCurrent,
                              LocalDate formulaDate) {
    public static FormulaResponse from(OpticalFormula f) {
        return new FormulaResponse(f.id(), f.odSph(), f.odCyl(), f.odAxis(), f.oiSph(), f.oiCyl(), f.oiAxis(),
                f.add(), f.pupillaryDistance(), f.current(), f.formulaDate());
    }
}
