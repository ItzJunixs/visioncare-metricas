package co.visioncare.vcapi.facturacion.internal.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.visioncare.vcapi.facturacion.internal.service.GetInvoiceUseCase;
import co.visioncare.vcapi.facturacion.internal.service.ListInvoicesUseCase;
import co.visioncare.vcapi.facturacion.internal.service.RegisterPaymentUseCase;
import co.visioncare.vcapi.facturacion.internal.dto.*;

/** HU-10 — see vc-docs/07-api/contracts/openapi/facturacion-api.yaml. */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final ListInvoicesUseCase list;
    private final GetInvoiceUseCase get;
    private final RegisterPaymentUseCase registerPayment;

    public InvoiceController(ListInvoicesUseCase list, GetInvoiceUseCase get, RegisterPaymentUseCase registerPayment) {
        this.list = list;
        this.get = get;
        this.registerPayment = registerPayment;
    }

    @GetMapping
    public List<InvoiceResponse> list(@RequestParam(required = false) String status) {
        return list.execute(status).stream().map(InvoiceResponse::from).toList();
    }

    @GetMapping("/{id}")
    public InvoiceDetailResponse byId(@PathVariable UUID id) {
        return InvoiceDetailResponse.from(get.execute(id));
    }

    @PostMapping("/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceDetailResponse pay(@PathVariable UUID id, @RequestBody @jakarta.validation.Valid NewPaymentRequest r) {
        return InvoiceDetailResponse.from(registerPayment.execute(id, r.amount(), r.method()));
    }

}
