package co.visioncare.vcapi.metricas;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import co.visioncare.vcapi.metricas.ElementosCodigo.MetodoJava;
import co.visioncare.vcapi.metricas.ElementosCodigo.TipoJava;

/**
 * Orquesta el ciclo MEDIR → DETECTAR del módulo: recorre {@code src/main/java}, calcula V(G), LOC,
 * Halstead, MI y la suite CK, y escribe los resultados en CSV y en un resumen Markdown.
 *
 * <pre>
 *   Desde vc-api:  mvn -q test -Dtest=MetricasMantenibilidadTest
 *   Resultados:    target/metricas/{metodos.csv, clases-ck.csv, archivos-loc.csv, resumen.md}
 * </pre>
 */
public final class ReporteMetricas {

    public record FilaMetodo(String archivo, String clase, String modulo, String metodo, int linea, int cc,
                             int nloc, int parametros, double volumen, double mi) {
    }

    public record FilaArchivo(String archivo, String modulo, MetricasLOC loc) {
    }

    private final List<FilaMetodo> metodos = new ArrayList<>();
    private final List<FilaArchivo> archivos = new ArrayList<>();
    private final List<TipoJava> tipos = new ArrayList<>();
    private List<ClaseCK> clases = List.of();

    public static ReporteMetricas medir(Path raiz) throws IOException {
        ReporteMetricas r = new ReporteMetricas();
        List<Path> fuentes;
        try (Stream<Path> s = Files.walk(raiz)) {
            fuentes = s.filter(p -> p.toString().endsWith(".java")).sorted().toList();
        }
        for (Path p : fuentes) {
            String codigo = Files.readString(p, StandardCharsets.UTF_8).replace("\r\n", "\n");
            String relativo = raiz.relativize(p).toString().replace('\\', '/');
            AnalizadorLexico lexico = new AnalizadorLexico(codigo);
            List<TipoJava> enArchivo = AnalizadorEstructura.analizar(lexico.tokens(), relativo);
            r.tipos.addAll(enArchivo);
            String modulo = enArchivo.isEmpty() ? "(raiz)" : enArchivo.get(0).modulo();
            r.archivos.add(new FilaArchivo(relativo, modulo, MetricasLOC.de(codigo, lexico)));
            for (TipoJava tipo : enArchivo) {
                for (MetodoJava m : tipo.metodos) {
                    if (!m.tieneCuerpo) continue;
                    int cc = ComplejidadCiclomatica.calcular(m.tokens);
                    double v = Halstead.de(m.tokens).volumen();
                    r.metodos.add(new FilaMetodo(relativo, tipo.nombre, tipo.modulo(), m.nombre, m.lineaInicio(),
                            cc, m.nloc(), m.parametros, v, IndiceMantenibilidad.calcular(v, cc, m.nloc())));
                }
            }
        }
        r.clases = SuiteCK.medir(r.tipos);
        return r;
    }

    public List<FilaMetodo> metodos() { return metodos; }
    public List<FilaArchivo> archivos() { return archivos; }
    public List<ClaseCK> clases() { return clases; }

    // ------------------------------------------------------------------ salida

    public void escribir(Path destino) throws IOException {
        Files.createDirectories(destino);
        StringBuilder m = new StringBuilder("archivo,clase,modulo,metodo,linea,cc,riesgo,nloc,parametros,volumen_halstead,mi\n");
        for (FilaMetodo f : metodos) {
            m.append(String.format(Locale.ROOT, "%s,%s,%s,%s,%d,%d,%s,%d,%d,%.1f,%.1f%n", f.archivo(), f.clase(),
                    f.modulo(), f.metodo(), f.linea(), f.cc(), ComplejidadCiclomatica.clasificarRiesgo(f.cc()),
                    f.nloc(), f.parametros(), f.volumen(), f.mi()));
        }
        Files.writeString(destino.resolve("metodos.csv"), m);

        StringBuilder c = new StringBuilder("clase,tipo,modulo,capa,wmc,dit,noc,cbo,cbo_proyecto,rfc,lcom,lcom4,metodos,campos,nloc,mi,problematica,recomendacion\n");
        for (ClaseCK k : clases) {
            c.append(String.format(Locale.ROOT, "%s,%s,%s,%s,%d,%d,%d,%d,%d,%d,%.2f,%d,%d,%d,%d,%s,%s,\"%s\"%n",
                    k.nombre(), k.clase(), k.modulo(), k.capa(), k.wmc(), k.dit(), k.noc(), k.cbo(), k.cboProyecto(),
                    k.rfc(), k.lcom(), k.lcom4(), k.metodos(), k.campos(), k.nloc(),
                    Double.isNaN(k.mi()) ? "" : String.format(Locale.ROOT, "%.1f", k.mi()),
                    k.esClaseProblematica() ? "si" : "no", String.join("; ", RecomendadorRefactorizacion.paraClase(k))));
        }
        Files.writeString(destino.resolve("clases-ck.csv"), c);

        StringBuilder a = new StringBuilder("archivo,modulo,fisicas,efectivas,comentario,en_blanco,pct_comentarios\n");
        for (FilaArchivo f : archivos) {
            MetricasLOC l = f.loc();
            a.append(String.format(Locale.ROOT, "%s,%s,%d,%d,%d,%d,%.1f%n", f.archivo(), f.modulo(), l.fisicas(),
                    l.efectivas(), l.comentario(), l.enBlanco(), l.porcentajeComentarios()));
        }
        Files.writeString(destino.resolve("archivos-loc.csv"), a);
        Files.writeString(destino.resolve("resumen.md"), resumen());
    }

    public String resumen() {
        StringBuilder sb = new StringBuilder("# Resumen de metricas - vc-api\n\n");
        int fis = 0, efe = 0, com = 0, bla = 0;
        for (FilaArchivo f : archivos) {
            fis += f.loc().fisicas(); efe += f.loc().efectivas(); com += f.loc().comentario(); bla += f.loc().enBlanco();
        }
        sb.append(String.format(Locale.ROOT, "Archivos: %d | Tipos: %d | Metodos con cuerpo: %d%n%n", archivos.size(),
                clases.size(), metodos.size()));
        sb.append(String.format(Locale.ROOT, "LOC fisicas %d | efectivas %d | comentario %d (%.1f%%) | en blanco %d%n%n",
                fis, efe, com, fis == 0 ? 0 : com * 100.0 / fis, bla));

        sb.append("## Distribucion de V(G) por metodo\n\n| Riesgo | Metodos |\n|---|---|\n");
        Map<String, Long> dist = metodos.stream().collect(Collectors.groupingBy(
                f -> ComplejidadCiclomatica.clasificarRiesgo(f.cc()), LinkedHashMap::new, Collectors.counting()));
        for (String nivel : List.of("Bajo", "Moderado", "Alto", "Muy alto")) {
            sb.append("| ").append(nivel).append(" | ").append(dist.getOrDefault(nivel, 0L)).append(" |\n");
        }
        sb.append(String.format(Locale.ROOT, "%nV(G) promedio %.2f | maximo %d | NLOC por metodo promedio %.1f | maximo %d%n%n",
                metodos.stream().mapToInt(FilaMetodo::cc).average().orElse(0),
                metodos.stream().mapToInt(FilaMetodo::cc).max().orElse(0),
                metodos.stream().mapToInt(FilaMetodo::nloc).average().orElse(0),
                metodos.stream().mapToInt(FilaMetodo::nloc).max().orElse(0)));

        sb.append("## Top 10 metodos por V(G)\n\n| Metodo | V(G) | NLOC | Param. | MI |\n|---|---|---|---|---|\n");
        metodos.stream().sorted(Comparator.comparingInt(FilaMetodo::cc).reversed().thenComparing(FilaMetodo::nloc, Comparator.reverseOrder()))
                .limit(10).forEach(f -> sb.append(String.format(Locale.ROOT, "| %s.%s | %d | %d | %d | %.1f |%n",
                        f.clase(), f.metodo(), f.cc(), f.nloc(), f.parametros(), f.mi())));

        sb.append("\n## Metodos con mas de ").append(RecomendadorRefactorizacion.UMBRAL_PARAMETROS).append(" parametros\n\n");
        metodos.stream().filter(f -> f.parametros() > RecomendadorRefactorizacion.UMBRAL_PARAMETROS)
                .sorted(Comparator.comparingInt(FilaMetodo::parametros).reversed())
                .forEach(f -> sb.append("- ").append(f.clase()).append('.').append(f.metodo()).append(": ")
                        .append(f.parametros()).append('\n'));

        sb.append("\n## Suite CK - clases que superan algun umbral\n\n| Clase | WMC | DIT | NOC | CBO | RFC | LCOM* | LCOM4 | Recomendacion |\n|---|---|---|---|---|---|---|---|---|\n");
        clases.stream().filter(ClaseCK::esClaseProblematica)
                .sorted(Comparator.comparingInt(ClaseCK::cbo).reversed())
                .forEach(k -> sb.append(String.format(Locale.ROOT, "| %s | %d | %d | %d | %d | %d | %.2f | %d | %s |%n",
                        k.nombre(), k.wmc(), k.dit(), k.noc(), k.cbo(), k.rfc(), k.lcom(), k.lcom4(),
                        String.join("; ", RecomendadorRefactorizacion.paraClase(k)))));

        sb.append("\n## Promedios por modulo\n\n| Modulo | Tipos | WMC prom. | CBO prom. | LCOM prom. | MI prom. |\n|---|---|---|---|---|---|\n");
        clases.stream().collect(Collectors.groupingBy(ClaseCK::modulo)).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    List<ClaseCK> ks = e.getValue();
                    sb.append(String.format(Locale.ROOT, "| %s | %d | %.1f | %.1f | %.2f | %.1f |%n", e.getKey(), ks.size(),
                            ks.stream().mapToInt(ClaseCK::wmc).average().orElse(0),
                            ks.stream().mapToInt(ClaseCK::cbo).average().orElse(0),
                            ks.stream().mapToDouble(ClaseCK::lcom).average().orElse(0),
                            ks.stream().mapToDouble(ClaseCK::mi).filter(d -> !Double.isNaN(d)).average().orElse(Double.NaN)));
                });
        return sb.toString();
    }

    /** Permite ejecutar el medidor sin JUnit: {@code java ... ReporteMetricas src/main/java target/metricas}. */
    public static void main(String[] args) throws IOException {
        Path raiz = Path.of(args.length > 0 ? args[0] : "src/main/java");
        Path salida = Path.of(args.length > 1 ? args[1] : "target/metricas");
        ReporteMetricas r = medir(raiz);
        r.escribir(salida);
        System.out.println(r.resumen());
    }
}
