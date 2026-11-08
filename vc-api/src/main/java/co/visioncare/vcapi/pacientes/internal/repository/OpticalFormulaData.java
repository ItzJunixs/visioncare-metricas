package co.visioncare.vcapi.pacientes.internal.repository;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "optical_formulas", schema = "pacientes")
public class OpticalFormulaData {

    @Id
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "od_sph", nullable = false)
    private double odSph;
    @Column(name = "od_cyl", nullable = false)
    private double odCyl;
    @Column(name = "od_axis", nullable = false)
    private int odAxis;
    @Column(name = "oi_sph", nullable = false)
    private double oiSph;
    @Column(name = "oi_cyl", nullable = false)
    private double oiCyl;
    @Column(name = "oi_axis", nullable = false)
    private int oiAxis;
    @Column(name = "add_power")
    private Double add;
    @Column(name = "pupillary_distance")
    private Double pupillaryDistance;
    @Column(name = "is_current", nullable = false)
    private boolean current;
    @Column(name = "formula_date", nullable = false)
    private LocalDate formulaDate;

    protected OpticalFormulaData() {
    }

    public OpticalFormulaData(UUID id, UUID patientId, double odSph, double odCyl, int odAxis, double oiSph,
                              double oiCyl, int oiAxis, Double add, Double pd, boolean current, LocalDate formulaDate) {
        this.id = id;
        this.patientId = patientId;
        this.odSph = odSph;
        this.odCyl = odCyl;
        this.odAxis = odAxis;
        this.oiSph = oiSph;
        this.oiCyl = oiCyl;
        this.oiAxis = oiAxis;
        this.add = add;
        this.pupillaryDistance = pd;
        this.current = current;
        this.formulaDate = formulaDate;
    }

    public UUID getId() { return id; }
    public UUID getPatientId() { return patientId; }
    public double getOdSph() { return odSph; }
    public double getOdCyl() { return odCyl; }
    public int getOdAxis() { return odAxis; }
    public double getOiSph() { return oiSph; }
    public double getOiCyl() { return oiCyl; }
    public int getOiAxis() { return oiAxis; }
    public Double getAdd() { return add; }
    public Double getPupillaryDistance() { return pupillaryDistance; }
    public boolean isCurrent() { return current; }
    public void setCurrent(boolean current) { this.current = current; }
    public LocalDate getFormulaDate() { return formulaDate; }
}
