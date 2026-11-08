package co.visioncare.vcapi.metricas;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

/**
 * Convierte un archivo .java en una lista de {@link Token} descartando comentarios y
 * normalizando literales. Al mismo tiempo marca qué líneas tienen código y cuáles tienen
 * comentarios, que es la base de {@link MetricasLOC}.
 *
 * <p>Es un analizador léxico deliberadamente pequeño (no un compilador): basta para contar
 * puntos de decisión, líneas y operadores/operandos de Halstead de forma reproducible.
 */
public final class AnalizadorLexico {

    static final Set<String> PALABRAS_CLAVE = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
            "volatile", "while", "var", "record", "yield", "true", "false", "null");

    /** Operadores de varios caracteres, del más largo al más corto para que el emparejamiento sea voraz. */
    private static final String[] OPERADORES = {
            ">>>=", "<<=", ">>=", "...", "->", "::", "==", "!=", "<=", ">=", "&&", "||", "++", "--",
            "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^="};

    private final List<Token> tokens = new ArrayList<>();
    private final BitSet lineasConCodigo = new BitSet();
    private final BitSet lineasConComentario = new BitSet();
    private int totalLineas;

    public AnalizadorLexico(String fuente) {
        analizar(fuente);
    }

    public List<Token> tokens() { return tokens; }
    public BitSet lineasConCodigo() { return lineasConCodigo; }
    public BitSet lineasConComentario() { return lineasConComentario; }
    public int totalLineas() { return totalLineas; }

    private void analizar(String s) {
        int i = 0;
        int linea = 1;
        int n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            if (c == '\n') { linea++; i++; continue; }
            if (Character.isWhitespace(c)) { i++; continue; }

            // Comentario de línea
            if (c == '/' && i + 1 < n && s.charAt(i + 1) == '/') {
                lineasConComentario.set(linea);
                while (i < n && s.charAt(i) != '\n') i++;
                continue;
            }
            // Comentario de bloque (incluye Javadoc)
            if (c == '/' && i + 1 < n && s.charAt(i + 1) == '*') {
                i += 2;
                lineasConComentario.set(linea);
                while (i < n && !(s.charAt(i) == '*' && i + 1 < n && s.charAt(i + 1) == '/')) {
                    if (s.charAt(i) == '\n') { linea++; lineasConComentario.set(linea); }
                    i++;
                }
                i += 2;
                continue;
            }
            // Bloque de texto """ ... """
            if (s.startsWith("\"\"\"", i)) {
                int inicio = linea;
                i += 3;
                while (i < n && !s.startsWith("\"\"\"", i)) {
                    if (s.charAt(i) == '\n') { linea++; lineasConCodigo.set(linea); }
                    i++;
                }
                i += 3;
                agregar("STR", Token.Tipo.LITERAL, inicio);
                continue;
            }
            // Cadena o carácter
            if (c == '"' || c == '\'') {
                char cierre = c;
                i++;
                while (i < n && s.charAt(i) != cierre) {
                    if (s.charAt(i) == '\\') i++;
                    i++;
                }
                i++;
                agregar(cierre == '"' ? "STR" : "CHR", Token.Tipo.LITERAL, linea);
                continue;
            }
            // Identificadores y palabras clave
            if (Character.isJavaIdentifierStart(c)) {
                int j = i;
                while (j < n && Character.isJavaIdentifierPart(s.charAt(j))) j++;
                String palabra = s.substring(i, j);
                agregar(palabra, PALABRAS_CLAVE.contains(palabra) ? Token.Tipo.PALABRA_CLAVE : Token.Tipo.IDENTIFICADOR, linea);
                i = j;
                continue;
            }
            // Literales numéricos
            if (Character.isDigit(c) || (c == '.' && i + 1 < n && Character.isDigit(s.charAt(i + 1)))) {
                int j = i;
                while (j < n && (Character.isLetterOrDigit(s.charAt(j)) || s.charAt(j) == '.' || s.charAt(j) == '_')) j++;
                agregar(s.substring(i, j), Token.Tipo.LITERAL, linea);
                i = j;
                continue;
            }
            // Operadores y separadores
            String op = String.valueOf(c);
            for (String candidato : OPERADORES) {
                if (s.startsWith(candidato, i)) { op = candidato; break; }
            }
            agregar(op, Token.Tipo.SIMBOLO, linea);
            i += op.length();
        }
        totalLineas = s.isEmpty() ? 0 : (s.endsWith("\n") ? linea - 1 : linea);
    }

    private void agregar(String texto, Token.Tipo tipo, int linea) {
        tokens.add(new Token(texto, tipo, linea));
        lineasConCodigo.set(linea);
    }
}
