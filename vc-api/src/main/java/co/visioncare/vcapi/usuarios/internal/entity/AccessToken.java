package co.visioncare.vcapi.usuarios.internal.entity;

public record AccessToken(String token, String username, String name, Role role, long expiresInMinutes) {
}
