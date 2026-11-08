package co.visioncare.vcapi.pacientes.internal.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePatientRequest(@NotBlank String documentType, @NotBlank String documentNumber,
                                   @NotBlank String firstName, @NotBlank String lastName,
                                   @NotBlank String phone, String email, @NotBlank String eps) {
}
