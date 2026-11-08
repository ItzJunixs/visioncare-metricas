package co.visioncare.vcapi.ordenes.internal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.visioncare.vcapi.inventario.InventarioFacade;
import co.visioncare.vcapi.pacientes.PacientesFacade;
import co.visioncare.vcapi.ordenes.internal.service.AdvanceWorkOrderStatusUseCase;
import co.visioncare.vcapi.ordenes.internal.service.ApproveWorkOrderUseCase;
import co.visioncare.vcapi.ordenes.internal.service.CreateWorkOrderUseCase;
import co.visioncare.vcapi.ordenes.internal.service.GetWorkOrderUseCase;
import co.visioncare.vcapi.ordenes.internal.service.ListWorkOrdersUseCase;
import co.visioncare.vcapi.ordenes.internal.entity.OrderStatusHistoryRepository;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderRepository;
import co.visioncare.vcapi.shared.events.DomainEventPublisher;
import co.visioncare.vcapi.shared.tx.TransactionRunner;

@Configuration
public class OrdenesConfig {

    @Bean
    CreateWorkOrderUseCase createWorkOrderUseCase(WorkOrderRepository workOrders, PacientesFacade pacientes,
                                                  InventarioFacade inventario) {
        return new CreateWorkOrderUseCase(workOrders, pacientes, inventario);
    }

    @Bean
    ApproveWorkOrderUseCase approveWorkOrderUseCase(WorkOrderRepository workOrders, OrderStatusHistoryRepository history,
                                                    InventarioFacade inventario, TransactionRunner transaction,
                                                    DomainEventPublisher events) {
        return new ApproveWorkOrderUseCase(workOrders, history, inventario, transaction, events);
    }

    @Bean
    AdvanceWorkOrderStatusUseCase advanceWorkOrderStatusUseCase(WorkOrderRepository workOrders,
                                                                OrderStatusHistoryRepository history) {
        return new AdvanceWorkOrderStatusUseCase(workOrders, history);
    }

    @Bean
    ListWorkOrdersUseCase listWorkOrdersUseCase(WorkOrderRepository workOrders) {
        return new ListWorkOrdersUseCase(workOrders);
    }

    @Bean
    GetWorkOrderUseCase getWorkOrderUseCase(WorkOrderRepository workOrders, OrderStatusHistoryRepository history) {
        return new GetWorkOrderUseCase(workOrders, history);
    }
}
