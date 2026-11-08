package co.visioncare.vcapi.shared.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Driven adapter: publishes through Spring's {@link ApplicationEventPublisher}. A listener
 * declared with {@code @TransactionalEventListener(phase = AFTER_COMMIT)} only runs once the
 * publishing transaction has committed — so the reacting module never waits on, and never
 * shares a transaction with, the module that raised the event.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher delegate;

    public SpringDomainEventPublisher(ApplicationEventPublisher delegate) {
        this.delegate = delegate;
    }

    @Override
    public void publish(Object event) {
        delegate.publishEvent(event);
    }
}
