package co.visioncare.vcapi.inventario.internal.service;

import java.util.UUID;

import co.visioncare.vcapi.inventario.internal.entity.Frame;
import co.visioncare.vcapi.inventario.internal.entity.FrameRepository;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;

/** Read side of the frame CRUD: one frame by id. */
public class GetFrameUseCase {

    private final FrameRepository frames;

    public GetFrameUseCase(FrameRepository frames) {
        this.frames = frames;
    }

    public Frame execute(UUID id) {
        return frames.findById(id).orElseThrow(() -> new ResourceNotFoundException("Frame not found: " + id));
    }
}
