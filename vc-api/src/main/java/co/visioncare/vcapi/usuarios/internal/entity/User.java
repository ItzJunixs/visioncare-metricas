package co.visioncare.vcapi.usuarios.internal.entity;

import java.util.UUID;

/** User aggregate root. Pure domain object: no framework, no persistence. */
public class User {

    private final UUID id;
    private final String username;
    private final String passwordHash;
    private final String name;
    private final Role role;

    private User(UUID id, String username, String passwordHash, String name, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
    }

    public static User create(String username, String passwordHash, String name, Role role) {
        return new User(UUID.randomUUID(), username, passwordHash, name, role);
    }

    public static User rehydrate(UUID id, String username, String passwordHash, String name, Role role) {
        return new User(id, username, passwordHash, name, role);
    }

    public UUID id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public String name() {
        return name;
    }

    public Role role() {
        return role;
    }
}
