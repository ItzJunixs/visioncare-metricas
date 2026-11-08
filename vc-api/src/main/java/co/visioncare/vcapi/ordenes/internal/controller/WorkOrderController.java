package co.visioncare.vcapi.ordenes.internal.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import co.visioncare.vcapi.ordenes.internal.service.AdvanceWorkOrderStatusUseCase;
import co.visioncare.vcapi.ordenes.internal.service.ApproveWorkOrderUseCase;
import co.visioncare.vcapi.ordenes.internal.service.CreateWorkOrderUseCase;
import co.visioncare.vcapi.ordenes.internal.service.GetWorkOrderUseCase;
import co.visioncare.vcapi.ordenes.internal.service.ListWorkOrdersUseCase;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderStatus;
import co.visioncare.vcapi.ordenes.internal.dto.*;

/** HU-07, HU-08, HU-09 — see vc-docs/07-api/contracts/openapi/ordenes-api.yaml. */
@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final CreateWorkOrderUseCase create;
    private final ApproveWorkOrderUseCase approve;
    private final AdvanceWorkOrderStatusUseCase advance;
    private final ListWorkOrdersUseCase list;
    private final GetWorkOrderUseCase get;

    public WorkOrderController(CreateWorkOrderUseCase create, ApproveWorkOrderUseCase approve,
                               AdvanceWorkOrderStatusUseCase advance, ListWorkOrdersUseCase list,
                               GetWorkOrderUseCase get) {
        this.create = create;
        this.approve = approve;
        this.advance = advance;
        this.list = list;
        this.get = get;
    }

    @GetMapping
    public List<WorkOrderResponse> list(@RequestParam(required = false) String status) {
        WorkOrderStatus filter = status == null ? null : WorkOrderStatus.valueOf(status);
        return list.execute(filter).stream().map(WorkOrderResponse::from).toList();
    }

    @GetMapping("/{id}")
    public WorkOrderResponse byId(@PathVariable UUID id) {
        return WorkOrderResponse.from(get.execute(id));
    }

    @GetMapping("/{id}/status-history")
    public List<HistoryEntryResponse> history(@PathVariable UUID id) {
        return get.history(id).stream().map(HistoryEntryResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderResponse create(@RequestBody @jakarta.validation.Valid NewWorkOrderRequest r) {
        return WorkOrderResponse.from(create.execute(r.patientId(), r.frameId()));
    }

    @PostMapping("/{id}/approve")
    public WorkOrderResponse approve(@PathVariable UUID id, Authentication auth) {
        return WorkOrderResponse.from(approve.execute(id, username(auth)));
    }

    @PatchMapping("/{id}/status")
    public WorkOrderResponse advance(@PathVariable UUID id, @RequestBody StatusChangeRequest r, Authentication auth) {
        WorkOrderStatus target = WorkOrderStatus.valueOf(r.status());
        return WorkOrderResponse.from(advance.execute(id, target, r.reason(), username(auth)));
    }

    private String username(Authentication auth) {
        return auth == null ? "system" : auth.getName();
    }

}
