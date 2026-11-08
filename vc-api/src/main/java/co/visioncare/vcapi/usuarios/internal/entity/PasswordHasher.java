package co.visioncare.vcapi.usuarios.internal.entity;

public interface PasswordHasher {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String hash);
}
