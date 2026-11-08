package co.visioncare.vcapi.pacientes.internal.dto;

import java.time.LocalDate;

public record NewFormulaRequest(double odSph, double odCyl, int odAxis, double oiSph, double oiCyl,
                                int oiAxis, Double add, Double pupillaryDistance, Boolean isCurrent,
                                LocalDate formulaDate) {
}
