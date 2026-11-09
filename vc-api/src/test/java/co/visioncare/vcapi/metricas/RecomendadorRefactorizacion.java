package co.visioncare.vcapi.metricas;

import java.util.ArrayList;
import java.util.List;

/**
 * Traduce métricas en técnicas de refactorización (tabla 4.2 del módulo). Solo PROPONE: el
 * medidor no modifica el código de la aplicación.
 */
public final class RecomendadorRefactorizacion {

    public static final int UMBRAL_CC = 10;
    public static final int UMBRAL_LOC_METODO = 50;
    public static final int UMBRAL_PARAMETROS = 5;

    private RecomendadorRefactorizacion() {
    }

    public static List<String> paraMetodo(int cc, int loc, int parametros) {
        List<String> r = new ArrayList<>();
        if (cc > UMBRAL_CC) r.add("Extract Method / Decompose Conditional (CC " + cc + " > " + UMBRAL_CC + ")");
        if (loc > UMBRAL_LOC_METODO) r.add("Extract Method (LOC " + loc + " > " + UMBRAL_LOC_METODO + ")");
        if (parametros > UMBRAL_PARAMETROS) r.add("Introduce Parameter Object / Builder (" + parametros + " parametros)");
        return r;
    }

    public static List<String> paraClase(ClaseCK c) {
        List<String> r = new ArrayList<>();
        if (c.esGodClass()) r.add("God Class: Extract Class por responsabilidad");
        if (c.superaWmc()) r.add("Extract Class / Extract Method (WMC " + c.wmc() + ")");
        if (c.superaLcom()) r.add("Extract Class / Move Method (LCOM* " + String.format(java.util.Locale.ROOT, "%.2f", c.lcom()) + ", LCOM4 " + c.lcom4() + " grupos)");
        if (c.superaCbo()) r.add("Facade o inyeccion de una abstraccion (CBO " + c.cbo() + ")");
        if (c.superaRfc()) r.add("Reducir la interfaz publica (RFC " + c.rfc() + ")");
        if (c.superaDit()) r.add("Revisar la jerarquia: aplanarla o Replace Inheritance with Delegation (DIT " + c.dit() + ")");
        return r;
    }
}
