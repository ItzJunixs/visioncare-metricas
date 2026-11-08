package co.visioncare.vcapi.usuarios.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserData, UUID> {
    Optional<UserData> findByUsername(String username);
}
