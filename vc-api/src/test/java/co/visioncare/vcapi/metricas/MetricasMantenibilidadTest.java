package co.visioncare.vcapi.metricas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.visioncare.vcapi.metricas.ElementosCodigo.TipoJava;

/**
 * Proyecto final de Métricas y Estándares de Calidad: medición de la mantenibilidad de vc-api.
 *
 * <p>Este test NO modifica la aplicación. Mide {@code src/main/java}, deja los resultados en
 * {@code target/metricas/} y verifica el requisito no funcional de mantenibilidad propuesto
 * (RNF-03: ningún método supera V(G) = 10). Las clases que superan umbrales CK se reportan,
 * no hacen fallar el build: son insumo del plan de mejoras.
 *
 * <pre>mvn -q test -Dtest=MetricasMantenibilidadTest</pre>
 */
class MetricasMantenibilidadTest {

    private static ReporteMetricas reporte;

    @BeforeAll
    static void medir() throws IOException {
        reporte = ReporteMetricas.medir(Path.of("src/main/java"));
        reporte.escribir(Path.of("target/metricas"));
    }

    // ------------------------------------------------- calibración contra el material de clase

    @Test
    @DisplayName("Calibracion: GestorPedidosAntes.procesar() del modulo tiene V(G) = 11")
    void calibraComplejidadConElEjemploDeClase() {
        String antes = """
                class GestorPedidosAntes {
                    public String procesar(String tipoCliente, double monto, boolean tieneCupon,
                                           boolean esPrimeraCompra, String pais) {
                        double descuento = 0;
                        if (tipoCliente.equals("VIP")) {
                            if (monto > 1000) { descuento = 0.20; }
                            else if (monto > 500) { descuento = 0.15; }
                            else { descuento = 0.10; }
                        } else if (tipoCliente.equals("FRECUENTE")) {
                            if (monto > 1000) { descuento = 0.12; } else { descuento = 0.08; }
                        } else {
                            if (esPrimeraCompra) { descuento = 0.05; }
                        }
                        if (tieneCupon) { descuento += 0.03; }
                        if (pais.equals("CO")) { descuento += 0.0; }
                        else if (pais.equals("MX")) { descuento += 0.01; }
                        else { descuento += 0.02; }
                        if (descuento > 0.35) { descuento = 0.35; }
                        double total = monto - (monto * descuento);
                        return String.format("Total a pagar: %.2f", total);
                    }
                }
                """;
        TipoJava tipo = AnalizadorEstructura.analizar(new AnalizadorLexico(antes).tokens(), "ejemplo").get(0);
        assertEquals(11, ComplejidadCiclomatica.calcular(tipo.metodos.get(0).tokens));
    }

    @Test
    @DisplayName("Calibracion: el fragmento sumar() del modulo tiene 4 LOC fisicas, 3 efectivas y 1 de comentario")
    void calibraLocConElEjemploDeClase() {
        String fragmento = "public int sumar(int a, int b) {\n    // suma dos enteros\n    return a + b;\n}\n";
        MetricasLOC loc = MetricasLOC.de(fragmento, new AnalizadorLexico(fragmento));
        assertEquals(4, loc.fisicas());
        assertEquals(3, loc.efectivas());
        assertEquals(1, loc.comentario());
    }

    // ------------------------------------------------- medición de vc-api

    @Test
    @DisplayName("Mide todo src/main/java y genera target/metricas")
    void generaReporte() {
        assertFalse(reporte.metodos().isEmpty(), "No se encontraron metodos para medir");
        assertFalse(reporte.clases().isEmpty(), "No se encontraron clases para medir");
        System.out.println(reporte.resumen());
    }

    @Test
    @DisplayName("RNF-03: ningun metodo del backend supera V(G) = 10")
    void rnf03ComplejidadCiclomaticaMaxima() {
        List<String> infractores = reporte.metodos().stream()
                .filter(m -> m.cc() > RecomendadorRefactorizacion.UMBRAL_CC)
                .map(m -> m.clase() + "." + m.metodo() + " V(G)=" + m.cc())
                .collect(Collectors.toList());
        assertTrue(infractores.isEmpty(), "Metodos sobre el umbral: " + infractores);
    }

    @Test
    @DisplayName("Suite CK: reporta (sin fallar) las clases candidatas a refactorizacion")
    void reportaClasesCandidatas() {
        List<ClaseCK> candidatas = reporte.clases().stream().filter(ClaseCK::esClaseProblematica).toList();
        candidatas.forEach(c -> System.out.println(c.nombre() + " -> " + RecomendadorRefactorizacion.paraClase(c)));
        assertTrue(reporte.clases().stream().noneMatch(ClaseCK::esGodClass),
                "Se detecto una God Class (WMC>20, CBO>10 y LCOM confirmado a la vez)");
    }
}
