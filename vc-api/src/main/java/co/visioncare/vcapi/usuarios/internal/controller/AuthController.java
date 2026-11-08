package co.visioncare.vcapi.usuarios.internal.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.visioncare.vcapi.usuarios.internal.service.AuthenticateUseCase;
import co.visioncare.vcapi.usuarios.internal.entity.AccessToken;
import jakarta.validation.Valid;
import co.visioncare.vcapi.usuarios.internal.dto.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticateUseCase authenticate;

    public AuthController(AuthenticateUseCase authenticate) {
        this.authenticate = authenticate;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        AccessToken token = authenticate.execute(request.username(), request.password());
        return new LoginResponse(token.token(), token.username(), token.name(),
                token.role().name(), token.expiresInMinutes());
    }

}
