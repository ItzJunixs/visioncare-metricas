package co.visioncare.vcapi.facturacion.internal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.visioncare.vcapi.facturacion.internal.service.CreateInvoiceUseCase;
import co.visioncare.vcapi.facturacion.internal.service.GetInvoiceUseCase;
import co.visioncare.vcapi.facturacion.internal.service.ListInvoicesUseCase;
import co.visioncare.vcapi.facturacion.internal.service.RegisterPaymentUseCase;
import co.visioncare.vcapi.facturacion.internal.entity.InvoiceRepository;

@Configuration
public class FacturacionConfig {

    @Bean
    CreateInvoiceUseCase createInvoiceUseCase(InvoiceRepository invoices) {
        return new CreateInvoiceUseCase(invoices);
    }

    @Bean
    RegisterPaymentUseCase registerPaymentUseCase(InvoiceRepository invoices) {
        return new RegisterPaymentUseCase(invoices);
    }

    @Bean
    ListInvoicesUseCase listInvoicesUseCase(InvoiceRepository invoices) {
        return new ListInvoicesUseCase(invoices);
    }

    @Bean
    GetInvoiceUseCase getInvoiceUseCase(InvoiceRepository invoices) {
        return new GetInvoiceUseCase(invoices);
    }
}
