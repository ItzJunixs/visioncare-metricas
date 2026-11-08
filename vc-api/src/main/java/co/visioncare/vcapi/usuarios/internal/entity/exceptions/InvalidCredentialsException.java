package co.visioncare.vcapi.usuarios.internal.entity.exceptions;

import co.visioncare.vcapi.shared.exceptions.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
