package co.visioncare.vcapi.usuarios.internal.dto;

public record LoginResponse(String token, String username, String name, String role, long expiresInMinutes) {
}
