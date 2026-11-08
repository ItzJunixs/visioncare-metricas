package co.visioncare.vcapi.usuarios.internal.entity;

import java.util.Optional;

public interface TokenProvider {
    String generateFor(User user);

    Optional<Principal> verify(String token);

    long expirationMinutes();

    record Principal(String username, Role role) {
    }
}
