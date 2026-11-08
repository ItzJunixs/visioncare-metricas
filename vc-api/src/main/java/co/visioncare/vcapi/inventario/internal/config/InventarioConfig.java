package co.visioncare.vcapi.inventario.internal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.visioncare.vcapi.inventario.internal.service.DeleteFrameUseCase;
import co.visioncare.vcapi.inventario.internal.service.GetFrameUseCase;
import co.visioncare.vcapi.inventario.internal.service.ListFramesUseCase;
import co.visioncare.vcapi.inventario.internal.service.UpdateFrameUseCase;
import co.visioncare.vcapi.shared.tx.TransactionRunner;
import co.visioncare.vcapi.inventario.internal.service.RegisterFrameUseCase;
import co.visioncare.vcapi.inventario.internal.service.SetMinStockUseCase;
import co.visioncare.vcapi.inventario.internal.entity.FrameRepository;
import co.visioncare.vcapi.inventario.internal.entity.StockMovementRepository;

@Configuration
public class InventarioConfig {

    @Bean
    RegisterFrameUseCase registerFrameUseCase(FrameRepository frames, StockMovementRepository movements) {
        return new RegisterFrameUseCase(frames, movements);
    }

    @Bean
    ListFramesUseCase listFramesUseCase(FrameRepository frames) {
        return new ListFramesUseCase(frames);
    }

    @Bean
    SetMinStockUseCase setMinStockUseCase(FrameRepository frames) {
        return new SetMinStockUseCase(frames);
    }

    @Bean
    GetFrameUseCase getFrameUseCase(FrameRepository frames) {
        return new GetFrameUseCase(frames);
    }

    @Bean
    UpdateFrameUseCase updateFrameUseCase(FrameRepository frames) {
        return new UpdateFrameUseCase(frames);
    }

    @Bean
    DeleteFrameUseCase deleteFrameUseCase(FrameRepository frames, StockMovementRepository movements,
                                          TransactionRunner transaction) {
        return new DeleteFrameUseCase(frames, movements, transaction);
    }
}
