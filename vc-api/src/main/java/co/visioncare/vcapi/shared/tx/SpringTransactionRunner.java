package co.visioncare.vcapi.shared.tx;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class SpringTransactionRunner implements TransactionRunner {

    private final TransactionTemplate template;

    public SpringTransactionRunner(TransactionTemplate template) {
        this.template = template;
    }

    @Override
    public <T> T execute(Supplier<T> work) {
        return template.execute(status -> work.get());
    }
}
