package co.visioncare.vcapi.pacientes.internal.dto;

import java.time.LocalDate;

public record PeriodicControl(LocalDate nextControlDate, String status) {
}
