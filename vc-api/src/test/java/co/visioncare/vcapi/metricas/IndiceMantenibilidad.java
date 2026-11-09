package co.visioncare.vcapi.metricas;

/**
 * Índice de Mantenibilidad de Oman y Hagemeister (sección 2.4 del módulo):
 * <pre>MI = 171 − 5.2·ln(HV) − 0.23·CC − 16.2·ln(LOC)</pre>
 * Para una clase se usan los promedios por método de HV, CC y LOC efectivas (variante usual
 * del SEI). También se entrega la versión normalizada 0–100 de Visual Studio:
 * {@code max(0, MI · 100 / 171)}.
 *
 * <p>Interpretación (escala original): ≥ 85 alta, 65–85 moderada, &lt; 65 baja.
 */
public final class IndiceMantenibilidad {

    private IndiceMantenibilidad() {
    }

    public static double calcular(double volumenHalstead, double complejidad, double loc) {
        double hv = Math.max(volumenHalstead, 1);
        double lineas = Math.max(loc, 1);
        return 171 - 5.2 * Math.log(hv) - 0.23 * complejidad - 16.2 * Math.log(lineas);
    }

    public static double normalizado(double mi) {
        return Math.max(0, mi * 100 / 171);
    }

    public static String clasificar(double mi) {
        if (mi >= 85) return "Alta";
        if (mi >= 65) return "Moderada";
        return "Baja";
    }
}
