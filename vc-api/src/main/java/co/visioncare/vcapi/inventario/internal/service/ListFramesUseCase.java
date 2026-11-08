package co.visioncare.vcapi.inventario.internal.service;

import java.util.List;

import co.visioncare.vcapi.inventario.internal.entity.Frame;
import co.visioncare.vcapi.inventario.internal.entity.FrameRepository;

/** HU-06 read side: list frames, optionally only those at or below their minimum stock. */
public class ListFramesUseCase {

    private final FrameRepository frames;

    public ListFramesUseCase(FrameRepository frames) {
        this.frames = frames;
    }

    public List<Frame> execute(boolean lowStockOnly) {
        return frames.findAll(lowStockOnly);
    }
}
