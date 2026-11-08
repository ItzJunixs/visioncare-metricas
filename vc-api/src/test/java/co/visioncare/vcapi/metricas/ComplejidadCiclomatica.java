package co.visioncare.vcapi.metricas;

import java.util.List;
import java.util.Set;

/**
 * Complejidad ciclomática de McCabe con la fórmula práctica del módulo:
 * <pre>V(G) = puntos de decisión + 1</pre>
 * Puntos de decisión: {@code if, for, while, case, catch, &&, ||} y el operador ternario {@code ?}
 * (el comodín de genéricos {@code <?>} no se cuenta). {@code else} y {@code default} no suman
 * porque no abren un camino independiente nuevo. Es el mismo criterio que usa lizard, lo que
 * permite validar este medidor contra una herramienta externa.
 */
public final class ComplejidadCiclomatica {

    private static final Set<String> DECISIONES = Set.of("if", "for", "while", "case", "catch", "&&", "||");

    private ComplejidadCiclomatica() {
    }

    public static int calcular(List<Token> tokens) {
        int puntos = 0;
        for (int i = 0; i < tokens.size(); i++) {
            Token tk = tokens.get(i);
            if (DECISIONES.contains(tk.texto())) puntos++;
            else if (tk.es("?") && !esComodinGenerico(tokens, i)) puntos++;
        }
        return puntos + 1;
    }

    private static boolean esComodinGenerico(List<Token> tokens, int i) {
        String anterior = i > 0 ? tokens.get(i - 1).texto() : "";
        String siguiente = i + 1 < tokens.size() ? tokens.get(i + 1).texto() : "";
        return anterior.equals("<") || anterior.equals(",")
                || siguiente.equals("extends") || siguiente.equals("super") || siguiente.equals(">");
    }

    /** Niveles de riesgo de la tabla 2.3 del módulo. */
    public static String clasificarRiesgo(int cc) {
        if (cc <= 10) return "Bajo";
        if (cc <= 20) return "Moderado";
        if (cc <= 50) return "Alto";
        return "Muy alto";
    }
}
