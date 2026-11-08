package co.visioncare.vcapi.metricas;

import java.util.BitSet;

/**
 * Variantes de LOC de un archivo (sección 2.1 del módulo):
 * <ul>
 *   <li><b>físicas</b>: todas las líneas del archivo;</li>
 *   <li><b>efectivas (SLOC)</b>: líneas que contienen al menos un token de código;</li>
 *   <li><b>comentario</b>: líneas que solo contienen comentario (una línea con código y un
 *       comentario al final cuenta como efectiva, no como comentario);</li>
 *   <li><b>en blanco</b>: líneas vacías o solo con espacios.</li>
 * </ul>
 * A diferencia del ejemplo de clase, no se equivoca con una línea como {@code int x = 1; // nota}
 * porque se apoya en el {@link AnalizadorLexico}.
 */
public record MetricasLOC(int fisicas, int efectivas, int comentario, int enBlanco) {

    public static MetricasLOC de(String fuente, AnalizadorLexico lexico) {
        String[] lineas = fuente.split("\n", -1);
        int fisicas = lexico.totalLineas();
        BitSet codigo = lexico.lineasConCodigo();
        BitSet comentario = lexico.lineasConComentario();
        int efectivas = 0, soloComentario = 0, blanco = 0;
        for (int l = 1; l <= fisicas; l++) {
            if (codigo.get(l)) efectivas++;
            else if (comentario.get(l)) soloComentario++;
            else if (lineas[l - 1].isBlank()) blanco++;
        }
        return new MetricasLOC(fisicas, efectivas, soloComentario, blanco);
    }

    public double porcentajeComentarios() {
        return fisicas == 0 ? 0 : comentario * 100.0 / fisicas;
    }
}
