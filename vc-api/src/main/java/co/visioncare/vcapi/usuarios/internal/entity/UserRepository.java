package co.visioncare.vcapi.usuarios.internal.entity;

import java.util.Optional;

/** Outbound port for user persistence. Implemented by a driven adapter. */
public interface UserRepository {
    Optional<User> findByUsername(String username);

    User save(User user);

    boolean anyExists();
}
