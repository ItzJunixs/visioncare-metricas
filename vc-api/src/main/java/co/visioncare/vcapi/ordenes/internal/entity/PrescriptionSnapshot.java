package co.visioncare.vcapi.ordenes.internal.entity;

/** Copied from pacientes through PacientesFacade at creation time (06-data/models.md) — never a live join. */
public record PrescriptionSnapshot(double odSph, double odCyl, int odAxis, double oiSph, double oiCyl,
                                   int oiAxis, Double add, Double pupillaryDistance) {
}
