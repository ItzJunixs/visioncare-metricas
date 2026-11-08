package co.visioncare.vcapi.shared.tx;

import java.util.function.Supplier;

/**
 * Outbound port to run a unit of work atomically, without the use case knowing about
 * any transaction framework. This is how BR-09 commits the work-order approval and the
 * stock decrement together — two facade calls inside one {@link #execute} call.
 */
public interface TransactionRunner {
    <T> T execute(Supplier<T> work);
}
