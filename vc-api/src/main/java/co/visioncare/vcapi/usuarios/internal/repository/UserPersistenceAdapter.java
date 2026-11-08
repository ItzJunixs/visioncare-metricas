package co.visioncare.vcapi.usuarios.internal.repository;

import java.util.Optional;

import org.springframework.stereotype.Component;

import co.visioncare.vcapi.usuarios.internal.entity.Role;
import co.visioncare.vcapi.usuarios.internal.entity.User;
import co.visioncare.vcapi.usuarios.internal.entity.UserRepository;

@Component
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    public UserPersistenceAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(UserPersistenceAdapter::toDomain);
    }

    @Override
    public User save(User user) {
        UserData saved = jpa.save(new UserData(user.id(), user.username(), user.passwordHash(),
                user.name(), user.role().name()));
        return toDomain(saved);
    }

    @Override
    public boolean anyExists() {
        return jpa.count() > 0;
    }

    private static User toDomain(UserData d) {
        return User.rehydrate(d.getId(), d.getUsername(), d.getPasswordHash(), d.getName(), Role.valueOf(d.getRole()));
    }
}
