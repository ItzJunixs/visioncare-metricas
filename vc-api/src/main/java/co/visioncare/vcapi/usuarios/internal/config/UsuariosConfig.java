package co.visioncare.vcapi.usuarios.internal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.visioncare.vcapi.usuarios.internal.service.AuthenticateUseCase;
import co.visioncare.vcapi.usuarios.internal.entity.PasswordHasher;
import co.visioncare.vcapi.usuarios.internal.entity.TokenProvider;
import co.visioncare.vcapi.usuarios.internal.entity.UserRepository;

/** Wires the framework-free usuarios use cases as Spring beans. */
@Configuration
public class UsuariosConfig {

    @Bean
    AuthenticateUseCase authenticateUseCase(UserRepository users, PasswordHasher hasher, TokenProvider tokens) {
        return new AuthenticateUseCase(users, hasher, tokens);
    }
}
