package co.visioncare.vcapi.shared.events;

/**
 * Outbound port for an in-process domain event — "a module reacts to something that
 * already happened", per 05-architecture/modular-monolith.md. Never a broker or a queue:
 * synchronous, same process, same release.
 */
public interface DomainEventPublisher {
    void publish(Object event);
}
