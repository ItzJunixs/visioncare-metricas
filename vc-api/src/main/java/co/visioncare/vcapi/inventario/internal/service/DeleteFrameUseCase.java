package co.visioncare.vcapi.inventario.internal.service;

import java.util.UUID;

import co.visioncare.vcapi.inventario.internal.entity.FrameRepository;
import co.visioncare.vcapi.inventario.internal.entity.StockMovementRepository;
import co.visioncare.vcapi.shared.exceptions.ConflictException;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;
import co.visioncare.vcapi.shared.tx.TransactionRunner;

/**
 * Delete side of the frame CRUD. BR-07: a frame that has ever left stock keeps its history, so it
 * cannot be deleted. A frame with only entry movements is deleted together with them, atomically.
 */
public class DeleteFrameUseCase {

    private final FrameRepository frames;
    private final StockMovementRepository movements;
    private final TransactionRunner transaction;

    public DeleteFrameUseCase(FrameRepository frames, StockMovementRepository movements,
                              TransactionRunner transaction) {
        this.frames = frames;
        this.movements = movements;
        this.transaction = transaction;
    }

    public void execute(UUID id) {
        frames.findById(id).orElseThrow(() -> new ResourceNotFoundException("Frame not found: " + id));
        if (movements.hasExitMovements(id)) {
            throw new ConflictException("The frame already has exit movements and cannot be deleted");
        }
        transaction.execute(() -> {
            movements.deleteByFrameId(id);
            frames.deleteById(id);
            return null;
        });
    }
}
