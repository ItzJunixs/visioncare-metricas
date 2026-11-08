package co.visioncare.vcapi.inventario.internal.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.visioncare.vcapi.inventario.internal.service.DeleteFrameUseCase;
import co.visioncare.vcapi.inventario.internal.service.GetFrameUseCase;
import co.visioncare.vcapi.inventario.internal.service.ListFramesUseCase;
import co.visioncare.vcapi.inventario.internal.service.UpdateFrameUseCase;
import co.visioncare.vcapi.inventario.internal.service.RegisterFrameUseCase;
import co.visioncare.vcapi.inventario.internal.service.SetMinStockUseCase;
import co.visioncare.vcapi.inventario.internal.entity.Frame;
import jakarta.validation.Valid;
import co.visioncare.vcapi.inventario.internal.dto.*;

/** HU-05, HU-06 — see vc-docs/07-api/contracts/openapi/inventario-api.yaml. */
@RestController
@RequestMapping("/api/frames")
public class FrameController {

    private final RegisterFrameUseCase register;
    private final ListFramesUseCase list;
    private final GetFrameUseCase get;
    private final UpdateFrameUseCase update;
    private final DeleteFrameUseCase delete;
    private final SetMinStockUseCase setMinStock;

    public FrameController(RegisterFrameUseCase register, ListFramesUseCase list, GetFrameUseCase get,
                           UpdateFrameUseCase update, DeleteFrameUseCase delete, SetMinStockUseCase setMinStock) {
        this.register = register;
        this.list = list;
        this.get = get;
        this.update = update;
        this.delete = delete;
        this.setMinStock = setMinStock;
    }

    @GetMapping
    public List<FrameResponse> list(@RequestParam(required = false, defaultValue = "false") boolean lowStock) {
        return list.execute(lowStock).stream().map(FrameResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FrameResponse create(@RequestBody @Valid NewFrameRequest r) {
        Frame f = register.execute(new RegisterFrameUseCase.Command(r.sku(), r.brand(), r.model(), r.buyPrice(),
                r.sellPrice(), r.stockQuantity(), r.minStock() == null ? 0 : r.minStock()));
        return FrameResponse.from(f);
    }

    @PatchMapping("/{id}/min-stock")
    public FrameResponse setMinStock(@PathVariable UUID id, @RequestBody @Valid MinStockRequest r) {
        return FrameResponse.from(setMinStock.execute(id, r.minStock()));
    }

    @GetMapping("/{id}")
    public FrameResponse byId(@PathVariable UUID id) {
        return FrameResponse.from(get.execute(id));
    }

    @PutMapping("/{id}")
    public FrameResponse update(@PathVariable UUID id, @RequestBody @Valid NewFrameRequest r) {
        Frame f = update.execute(id, new RegisterFrameUseCase.Command(r.sku(), r.brand(), r.model(), r.buyPrice(),
                r.sellPrice(), r.stockQuantity(), r.minStock() == null ? 0 : r.minStock()));
        return FrameResponse.from(f);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        delete.execute(id);
    }
}
