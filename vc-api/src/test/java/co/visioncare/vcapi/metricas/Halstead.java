package co.visioncare.vcapi.metricas;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Volumen de Halstead, insumo del Índice de Mantenibilidad.
 * <pre>
 *   n = n1 + n2   (operadores y operandos distintos)
 *   N = N1 + N2   (ocurrencias totales)
 *   V = N · log2(n)
 * </pre>
 * Operadores: palabras clave y símbolos (los cierres ")", "]", "}" no se cuentan porque forman
 * pareja con su apertura). Operandos: identificadores y literales, incluidos {@code true},
 * {@code false}, {@code null} y {@code this}.
 */
public record Halstead(int n1, int n2, int totalOperadores, int totalOperandos) {

    private static final Set<String> OPERANDOS_CLAVE = Set.of("true", "false", "null", "this");
    private static final Set<String> CIERRES = Set.of(")", "]", "}");

    public static Halstead de(List<Token> tokens) {
        Set<String> operadores = new HashSet<>();
        Set<String> operandos = new HashSet<>();
        int totalOp = 0, totalOpnd = 0;
        for (Token tk : tokens) {
            boolean esOperando = tk.tipo() == Token.Tipo.IDENTIFICADOR || tk.tipo() == Token.Tipo.LITERAL
                    || OPERANDOS_CLAVE.contains(tk.texto());
            if (esOperando) {
                operandos.add(tk.texto());
                totalOpnd++;
            } else if (!CIERRES.contains(tk.texto())) {
                operadores.add(tk.texto());
                totalOp++;
            }
        }
        return new Halstead(operadores.size(), operandos.size(), totalOp, totalOpnd);
    }

    public double volumen() {
        int vocabulario = n1 + n2;
        int longitud = totalOperadores + totalOperandos;
        return vocabulario <= 1 ? 0 : longitud * (Math.log(vocabulario) / Math.log(2));
    }
}
