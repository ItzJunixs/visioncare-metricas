package co.visioncare.vcapi.inventario.internal.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.visioncare.vcapi.inventario.internal.entity.Frame;
import co.visioncare.vcapi.inventario.internal.entity.FrameRepository;
import co.visioncare.vcapi.inventario.internal.entity.Sku;
import co.visioncare.vcapi.inventario.internal.entity.StockMovementRepository;
import co.visioncare.vcapi.shared.exceptions.ConflictException;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;
import co.visioncare.vcapi.shared.money.Money;
import co.visioncare.vcapi.shared.tx.TransactionRunner;

/** BR-07: a frame that has left stock keeps its history; one that has not is removed with its entries. */
@ExtendWith(MockitoExtension.class)
class DeleteFrameUseCaseTest {

    @Mock
    private FrameRepository frames;
    @Mock
    private StockMovementRepository movements;
    @Mock
    private TransactionRunner transaction;

    private DeleteFrameUseCase useCase;
    private Frame frame;

    @BeforeEach
    void setUp() {
        useCase = new DeleteFrameUseCase(frames, movements, transaction);
        frame = Frame.register(new Sku("RB-1"), "Ray-Ban", "Aviator", Money.of(new BigDecimal("100000")),
                Money.of(new BigDecimal("200000")), 5, 2);
        lenient().when(transaction.execute(any())).thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());
    }

    @Test
    void deletesTheMovementsAndThenTheFrameWhenNothingEverLeftStock() {
        when(frames.findById(frame.id())).thenReturn(Optional.of(frame));
        when(movements.hasExitMovements(frame.id())).thenReturn(false);

        useCase.execute(frame.id());

        InOrder order = inOrder(movements, frames);
        order.verify(movements).deleteByFrameId(frame.id());
        order.verify(frames).deleteById(frame.id());
    }

    @Test
    void refusesToDeleteAFrameThatHasExitMovements() {
        when(frames.findById(frame.id())).thenReturn(Optional.of(frame));
        when(movements.hasExitMovements(frame.id())).thenReturn(true);

        assertThrows(ConflictException.class, () -> useCase.execute(frame.id()));

        verify(frames, never()).deleteById(any());
    }

    @Test
    void reportsAMissingFrame() {
        UUID id = UUID.randomUUID();
        when(frames.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(id));
    }
}
