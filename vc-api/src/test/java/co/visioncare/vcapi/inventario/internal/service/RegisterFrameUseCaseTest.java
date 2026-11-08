package co.visioncare.vcapi.inventario.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.visioncare.vcapi.inventario.internal.entity.Frame;
import co.visioncare.vcapi.inventario.internal.entity.FrameRepository;
import co.visioncare.vcapi.inventario.internal.entity.StockMovementRepository;
import co.visioncare.vcapi.inventario.internal.entity.exceptions.DuplicateSkuException;
import co.visioncare.vcapi.inventario.internal.entity.exceptions.InvalidPriceException;

/** HU-05: BR-04 (price), BR-05 (unique SKU) and BR-07 (every stock change has a movement). */
@ExtendWith(MockitoExtension.class)
class RegisterFrameUseCaseTest {

    @Mock
    private FrameRepository frames;
    @Mock
    private StockMovementRepository movements;

    private RegisterFrameUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterFrameUseCase(frames, movements);
    }

    private static RegisterFrameUseCase.Command command(String sku, String buy, String sell, int stock) {
        return new RegisterFrameUseCase.Command(sku, "Ray-Ban", "Aviator", new BigDecimal(buy),
                new BigDecimal(sell), stock, 2);
    }

    @Test
    void savesTheFrameAndRecordsAnEntryMovementForTheInitialStock() {
        when(frames.existsBySku("RB-1")).thenReturn(false);
        when(frames.save(any(Frame.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Frame saved = useCase.execute(command("RB-1", "100000", "200000", 5));

        assertEquals("RB-1", saved.sku().value());
        verify(movements).record(eq(saved.id()), eq("ENTRY"), eq(5), eq(null));
    }

    @Test
    void rejectsADuplicateSku() {
        when(frames.existsBySku("RB-1")).thenReturn(true);

        assertThrows(DuplicateSkuException.class, () -> useCase.execute(command("RB-1", "100000", "200000", 5)));

        verify(frames, never()).save(any(Frame.class));
        verify(movements, never()).record(any(), any(), anyInt(), any());
    }

    @Test
    void rejectsASellPriceBelowTheBuyPrice() {
        when(frames.existsBySku("RB-2")).thenReturn(false);

        assertThrows(InvalidPriceException.class, () -> useCase.execute(command("RB-2", "200000", "100000", 5)));

        verify(frames, never()).save(any(Frame.class));
    }
}
