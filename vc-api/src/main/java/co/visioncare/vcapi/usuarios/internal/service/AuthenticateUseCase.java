package co.visioncare.vcapi.usuarios.internal.service;

import co.visioncare.vcapi.usuarios.internal.entity.AccessToken;
import co.visioncare.vcapi.usuarios.internal.entity.PasswordHasher;
import co.visioncare.vcapi.usuarios.internal.entity.TokenProvider;
import co.visioncare.vcapi.usuarios.internal.entity.User;
import co.visioncare.vcapi.usuarios.internal.entity.UserRepository;
import co.visioncare.vcapi.usuarios.internal.entity.exceptions.InvalidCredentialsException;

public class AuthenticateUseCase {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final TokenProvider tokens;

    public AuthenticateUseCase(UserRepository users, PasswordHasher hasher, TokenProvider tokens) {
        this.users = users;
        this.hasher = hasher;
        this.tokens = tokens;
    }

    public AccessToken execute(String username, String password) {
        User user = users.findByUsername(username).orElseThrow(InvalidCredentialsException::new);
        if (!hasher.matches(password, user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        String token = tokens.generateFor(user);
        return new AccessToken(token, user.username(), user.name(), user.role(), tokens.expirationMinutes());
    }
}
