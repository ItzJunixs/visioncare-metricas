package co.visioncare.vcapi.pacientes.internal.entity;

import java.time.LocalDate;
import java.util.UUID;

import co.visioncare.vcapi.shared.exceptions.BusinessRuleException;

/**
 * Optical formula (HU-02). BR-02: axis 0–180 for OD and OI. BR-03: saving as current
 * supersedes the previous current formula — a state transition the use case orchestrates,
 * not something this aggregate enforces on itself.
 */
public class OpticalFormula {

    private final UUID id;
    private final UUID patientId;
    private final double odSph;
    private final double odCyl;
    private final int odAxis;
    private final double oiSph;
    private final double oiCyl;
    private final int oiAxis;
    private final Double add;
    private final Double pupillaryDistance;
    private final boolean current;
    private final LocalDate formulaDate;

    private OpticalFormula(UUID id, UUID patientId, double odSph, double odCyl, int odAxis,
                           double oiSph, double oiCyl, int oiAxis, Double add, Double pd,
                           boolean current, LocalDate formulaDate) {
        this.id = id;
        this.patientId = patientId;
        this.odSph = odSph;
        this.odCyl = odCyl;
        this.odAxis = requireAxis(odAxis);
        this.oiSph = oiSph;
        this.oiCyl = oiCyl;
        this.oiAxis = requireAxis(oiAxis);
        this.add = add;
        this.pupillaryDistance = pd;
        this.current = current;
        this.formulaDate = formulaDate == null ? LocalDate.now() : formulaDate;
    }

    public static OpticalFormula register(UUID patientId, double odSph, double odCyl, int odAxis,
                                          double oiSph, double oiCyl, int oiAxis, Double add, Double pd,
                                          boolean current, LocalDate formulaDate) {
        return new OpticalFormula(UUID.randomUUID(), patientId, odSph, odCyl, odAxis, oiSph, oiCyl, oiAxis,
                add, pd, current, formulaDate);
    }

    public static OpticalFormula rehydrate(UUID id, UUID patientId, double odSph, double odCyl, int odAxis,
                                           double oiSph, double oiCyl, int oiAxis, Double add, Double pd,
                                           boolean current, LocalDate formulaDate) {
        return new OpticalFormula(id, patientId, odSph, odCyl, odAxis, oiSph, oiCyl, oiAxis, add, pd,
                current, formulaDate);
    }

    /** Replaces the measured values and the current flag, keeping the identity (BR-02 is re-checked). */
    public OpticalFormula update(double newOdSph, double newOdCyl, int newOdAxis, double newOiSph, double newOiCyl,
                                 int newOiAxis, Double newAdd, Double newPd, boolean newCurrent,
                                 LocalDate newFormulaDate) {
        return new OpticalFormula(id, patientId, newOdSph, newOdCyl, newOdAxis, newOiSph, newOiCyl, newOiAxis,
                newAdd, newPd, newCurrent, newFormulaDate);
    }

    private static int requireAxis(int axis) {
        if (axis < 0 || axis > 180) {
            throw new BusinessRuleException("Axis must be between 0 and 180");
        }
        return axis;
    }

    public UUID id() {
        return id;
    }

    public UUID patientId() {
        return patientId;
    }

    public double odSph() {
        return odSph;
    }

    public double odCyl() {
        return odCyl;
    }

    public int odAxis() {
        return odAxis;
    }

    public double oiSph() {
        return oiSph;
    }

    public double oiCyl() {
        return oiCyl;
    }

    public int oiAxis() {
        return oiAxis;
    }

    public Double add() {
        return add;
    }

    public Double pupillaryDistance() {
        return pupillaryDistance;
    }

    public boolean current() {
        return current;
    }

    public LocalDate formulaDate() {
        return formulaDate;
    }

    /** HU-04: a formula older than 12 months marks the patient's control visit as overdue. */
    public boolean isOverdue() {
        return formulaDate.plusMonths(12).isBefore(LocalDate.now());
    }
}
