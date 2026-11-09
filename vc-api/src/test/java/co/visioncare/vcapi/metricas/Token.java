package co.visioncare.vcapi.metricas;

/**
 * Unidad léxica del código fuente ya sin comentarios.
 *
 * @param texto  texto del token (los literales de cadena se normalizan a "STR")
 * @param tipo   categoría léxica, usada por Halstead para separar operadores de operandos
 * @param linea  número de línea física (1-based) donde aparece el token
 */
public record Token(String texto, Tipo tipo, int linea) {

    public enum Tipo { IDENTIFICADOR, PALABRA_CLAVE, LITERAL, SIMBOLO }

    public boolean es(String valor) {
        return texto.equals(valor);
    }
}
