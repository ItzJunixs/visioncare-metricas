package co.visioncare.vcapi.inventario.internal.service;

import java.util.UUID;

import co.visioncare.vcapi.inventario.internal.entity.Frame;
import co.visioncare.vcapi.inventario.internal.entity.FrameRepository;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;

/** HU-06 write side: configure a frame's minimum stock alert threshold. */
public class SetMinStockUseCase {

    private final FrameRepository frames;

    public SetMinStockUseCase(FrameRepository frames) {
        this.frames = frames;
    }

    public Frame execute(UUID frameId, int minStock) {
        Frame frame = frames.findById(frameId)
                .orElseThrow(() -> new ResourceNotFoundException("Frame not found: " + frameId));
        frame.setMinStock(minStock);
        return frames.save(frame);
    }
}
