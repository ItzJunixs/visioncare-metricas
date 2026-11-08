package co.visioncare.vcapi.inventario.internal.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.visioncare.vcapi.inventario.internal.entity.Frame;
import co.visioncare.vcapi.inventario.internal.entity.Sku;
import co.visioncare.vcapi.inventario.internal.service.DeleteFrameUseCase;
import co.visioncare.vcapi.inventario.internal.service.GetFrameUseCase;
import co.visioncare.vcapi.inventario.internal.service.ListFramesUseCase;
import co.visioncare.vcapi.inventario.internal.service.RegisterFrameUseCase;
import co.visioncare.vcapi.inventario.internal.service.SetMinStockUseCase;
import co.visioncare.vcapi.inventario.internal.service.UpdateFrameUseCase;
import co.visioncare.vcapi.shared.exceptions.ConflictException;
import co.visioncare.vcapi.shared.money.Money;
import co.visioncare.vcapi.usuarios.internal.security.JwtAuthenticationFilter;

/**
 * Web layer only: the controller, the validation and the shared error handler, with the use cases
 * mocked. Security is covered by the Postman collection, which checks the 401 and 403 answers.
 */
@WebMvcTest(controllers = FrameController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class FrameControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RegisterFrameUseCase register;
    @MockitoBean
    private ListFramesUseCase list;
    @MockitoBean
    private GetFrameUseCase get;
    @MockitoBean
    private UpdateFrameUseCase update;
    @MockitoBean
    private DeleteFrameUseCase delete;
    @MockitoBean
    private SetMinStockUseCase setMinStock;

    private static Frame frame() {
        return Frame.register(new Sku("RB-1"), "Ray-Ban", "Aviator", Money.of(new BigDecimal("100000")),
                Money.of(new BigDecimal("200000")), 5, 2);
    }

    @Test
    void listsTheFramesAsDtos() throws Exception {
        when(list.execute(false)).thenReturn(List.of(frame()));

        mvc.perform(get("/api/frames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku", is("RB-1")))
                .andExpect(jsonPath("$[0].stockQuantity", is(5)));
    }

    @Test
    void createsAFrameAndAnswers201() throws Exception {
        when(register.execute(any(RegisterFrameUseCase.Command.class))).thenReturn(frame());

        mvc.perform(post("/api/frames").contentType(MediaType.APPLICATION_JSON).content("""
                        {"sku":"RB-1","brand":"Ray-Ban","model":"Aviator",
                         "buyPrice":100000,"sellPrice":200000,"stockQuantity":5,"minStock":2}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brand", is("Ray-Ban")));
    }

    @Test
    void answers400WithTheSharedErrorShapeWhenTheBodyIsInvalid() throws Exception {
        mvc.perform(post("/api/frames").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }

    @Test
    void deletesAFrameAndAnswers204() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(delete("/api/frames/" + id)).andExpect(status().isNoContent());

        verify(delete).execute(id);
    }

    @Test
    void answers409WhenTheFrameCannotBeDeleted() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ConflictException("The frame already has exit movements and cannot be deleted"))
                .when(delete).execute(id);

        mvc.perform(delete("/api/frames/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("CONFLICT")));
    }
}
