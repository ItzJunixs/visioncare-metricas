package co.visioncare.vcapi.metricas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.visioncare.vcapi.metricas.ElementosCodigo.MetodoJava;
import co.visioncare.vcapi.metricas.ElementosCodigo.TipoJava;

/**
 * Calcula las seis métricas de Chidamber y Kemerer para todos los tipos del sistema.
 *
 * <ul>
 *   <li><b>WMC</b>: suma de V(G) de los métodos y constructores de la clase.</li>
 *   <li><b>DIT</b>: 1 para una clase sin {@code extends} (solo hereda de Object); 1 + DIT del padre si
 *       el padre es del proyecto; para padres del JDK/Spring se usa su profundidad real conocida
 *       (p. ej. RuntimeException = 4) y 2 como cota inferior si el padre externo es desconocido.</li>
 *   <li><b>NOC</b>: clases del proyecto cuyo {@code extends} es esta clase.</li>
 *   <li><b>CBO</b>: tipos distintos que la clase usa (campos, parámetros, variables, {@code new},
 *       herencia, anotaciones), excluidos los de {@code java.*} y la propia clase.</li>
 *   <li><b>RFC</b>: métodos propios + métodos distintos invocados ({@code x.metodo(}, {@code X::metodo},
 *       {@code new X(}).</li>
 *   <li><b>LCOM*</b> (Henderson-Sellers): {@code (prom(μ(Aj)) − m) / (1 − m)}, con m = métodos de
 *       instancia (sin constructores) y μ(Aj) = métodos que usan el atributo j.</li>
 *   <li><b>LCOM4</b> (complemento): grupos de métodos conectados; confirma si un LCOM* alto es real.</li>
 * </ul>
 */
public final class SuiteCK {

    /** Tipos de java.lang que no aparecen en los import y no deben contar como acoplamiento. */
    private static final Set<String> JAVA_LANG = Set.of("String", "Integer", "Long", "Double", "Boolean",
            "Object", "Math", "Override", "Exception", "RuntimeException", "IllegalArgumentException",
            "IllegalStateException", "Character", "StringBuilder", "Void", "Iterable", "Record", "Enum",
            "Deprecated", "FunctionalInterface", "SuppressWarnings", "Runnable", "Throwable", "Class",
            "System", "Number", "Short", "Byte", "Float", "Thread", "Error", "Comparable",
            "UnsupportedOperationException", "NullPointerException", "SafeVarargs", "AutoCloseable");

    /** Profundidad real (Object = 1) de las superclases externas que usa el proyecto. */
    private static final java.util.Map<String, Integer> DIT_EXTERNO = java.util.Map.of(
            "Exception", 3, "RuntimeException", 4, "IllegalArgumentException", 5, "IllegalStateException", 5,
            "OncePerRequestFilter", 3, "Enum", 2, "Record", 2);

    private SuiteCK() {
    }

    public static List<ClaseCK> medir(List<TipoJava> tipos) {
        Map<String, TipoJava> porNombre = new HashMap<>();
        for (TipoJava tipo : tipos) porNombre.putIfAbsent(tipo.nombre, tipo);

        List<ClaseCK> resultado = new ArrayList<>();
        for (TipoJava tipo : tipos) {
            int wmc = 0;
            double sumaV = 0, sumaCC = 0, sumaLoc = 0;
            int conCuerpo = 0;
            for (MetodoJava m : tipo.metodos) {
                int cc = ComplejidadCiclomatica.calcular(m.tokens);
                wmc += cc;
                if (m.tieneCuerpo) {
                    conCuerpo++;
                    sumaCC += cc;
                    sumaLoc += m.nloc();
                    sumaV += Halstead.de(m.tokens).volumen();
                }
            }
            double mi = conCuerpo == 0 ? Double.NaN
                    : IndiceMantenibilidad.calcular(sumaV / conCuerpo, sumaCC / conCuerpo, sumaLoc / conCuerpo);
            int nloc = (int) tipo.tokens.stream().mapToInt(Token::linea).distinct().count();
            int[] cbo = cbo(tipo, porNombre);
            resultado.add(new ClaseCK(tipo.nombre, tipo.paquete, tipo.modulo(), tipo.capa(), tipo.clase,
                    wmc, dit(tipo, porNombre, 0), noc(tipo, tipos), cbo[0], cbo[1], rfc(tipo), lcom(tipo), lcom4(tipo),
                    tipo.metodos.size(), tipo.campos.size(), nloc, mi));
        }
        return resultado;
    }

    static int dit(TipoJava tipo, Map<String, TipoJava> porNombre, int guarda) {
        if (tipo.clase.equals("enum") || tipo.clase.equals("record")) return 2;
        if (tipo.superclase == null || tipo.clase.equals("interface")) return 1;
        TipoJava padre = porNombre.get(tipo.superclase);
        if (padre == null || guarda > 20) return 1 + DIT_EXTERNO.getOrDefault(tipo.superclase, 1);
        return 1 + dit(padre, porNombre, guarda + 1);
    }

    static int noc(TipoJava tipo, List<TipoJava> todos) {
        int hijos = 0;
        for (TipoJava otro : todos) {
            if (tipo.nombre.equals(otro.superclase)) hijos++;
        }
        return hijos;
    }

    /** @return {CBO total sin java.*, CBO solo con tipos del proyecto} */
    static int[] cbo(TipoJava tipo, Map<String, TipoJava> proyecto) {
        Set<String> total = new HashSet<>();
        Set<String> delProyecto = new HashSet<>();
        for (Token tk : tipo.tokens) {
            if (tk.tipo() != Token.Tipo.IDENTIFICADOR) continue;
            String nombre = tk.texto();
            if (nombre.equals(tipo.nombre) || !Character.isUpperCase(nombre.charAt(0))) continue;
            if (nombre.equals(nombre.toUpperCase()) && nombre.length() > 1) continue; // CONSTANTE
            String fqcn = tipo.imports.get(nombre);
            boolean esDelProyecto = proyecto.containsKey(nombre);
            boolean esJava = (fqcn != null && fqcn.startsWith("java.")) || (fqcn == null && JAVA_LANG.contains(nombre));
            if (esJava) continue;
            if (fqcn != null || esDelProyecto) {
                total.add(nombre);
                if (esDelProyecto || fqcn.startsWith("co.visioncare")) delProyecto.add(nombre);
            }
        }
        return new int[] {total.size(), delProyecto.size()};
    }

    static int rfc(TipoJava tipo) {
        Set<String> respuesta = new HashSet<>();
        for (MetodoJava m : tipo.metodos) respuesta.add("this." + m.nombre);
        List<Token> t = tipo.tokens;
        for (int i = 1; i + 1 < t.size(); i++) {
            Token tk = t.get(i);
            if (tk.tipo() != Token.Tipo.IDENTIFICADOR) continue;
            String anterior = t.get(i - 1).texto();
            boolean invocacion = anterior.equals(".") && t.get(i + 1).es("(");
            boolean referencia = anterior.equals("::");
            boolean constructor = anterior.equals("new");
            if (invocacion || referencia) respuesta.add("." + tk.texto());
            if (constructor) respuesta.add("new " + tk.texto());
        }
        return respuesta.size();
    }

    /** LCOM* de Henderson-Sellers (escala 0-1 del módulo), con métodos de instancia sin constructores. */
    static double lcom(TipoJava tipo) {
        List<String> atributos = atributosDeInstancia(tipo);
        List<MetodoJava> metodos = tipo.metodos.stream()
                .filter(m -> m.tieneCuerpo && !m.constructor && !m.estatico).toList();
        int m = metodos.size();
        int a = atributos.size();
        if (m <= 1 || a == 0) return 0;
        double sumaMu = 0;
        for (String atributo : atributos) {
            for (MetodoJava metodo : metodos) {
                if (usaAtributo(metodo, atributo)) sumaMu++;
            }
        }
        double valor = ((sumaMu / a) - m) / (1.0 - m);
        return Math.max(0, Math.min(1, valor));
    }

    /**
     * LCOM4 (Hitz y Montazeri): número de grupos de métodos conectados entre sí porque comparten un
     * atributo o porque uno invoca al otro. 1 = la clase tiene una sola responsabilidad; 2 o más =
     * hay grupos independientes que podrían ser clases distintas (Extract Class).
     *
     * <p>Se excluyen constructores y accesores triviales (getters/setters, {@code id()}): un accesor
     * toca un solo atributo por definición y hace que LCOM* marque como "no cohesivas" a entidades y
     * clases de datos que sí lo son. LCOM4 se usa para confirmar o descartar lo que indica LCOM*.
     */
    static int lcom4(TipoJava tipo) {
        List<String> atributos = atributosDeInstancia(tipo);
        List<MetodoJava> metodos = tipo.metodos.stream()
                .filter(m -> m.tieneCuerpo && !m.constructor && !m.estatico)
                .filter(m -> !esAccesor(m, atributos)).toList();
        int n = metodos.size();
        if (n == 0) return 0;
        int[] grupo = new int[n];
        for (int i = 0; i < n; i++) grupo[i] = i;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (conectados(metodos.get(i), metodos.get(j), atributos)) unir(grupo, i, j);
            }
        }
        Set<Integer> raices = new HashSet<>();
        for (int i = 0; i < n; i++) raices.add(raiz(grupo, i));
        return raices.size();
    }

    private static boolean conectados(MetodoJava a, MetodoJava b, List<String> atributos) {
        for (String atributo : atributos) {
            if (usaAtributo(a, atributo) && usaAtributo(b, atributo)) return true;
        }
        return invoca(a, b.nombre) || invoca(b, a.nombre);
    }

    private static boolean invoca(MetodoJava m, String nombre) {
        List<Token> t = m.tokens;
        for (int i = 1; i + 1 < t.size(); i++) { // desde 1: el token 0 puede ser el propio nombre
            if (!t.get(i).es(nombre) || !t.get(i + 1).es("(")) continue;
            boolean calificadoPorOtro = t.get(i - 1).es(".") && !(i > 1 && t.get(i - 2).es("this"));
            if (!calificadoPorOtro) return true;
        }
        return false;
    }

    private static int raiz(int[] g, int i) {
        while (g[i] != i) i = g[i];
        return i;
    }

    private static void unir(int[] g, int a, int b) {
        g[raiz(g, a)] = raiz(g, b);
    }

    private static List<String> atributosDeInstancia(TipoJava tipo) {
        List<String> atributos = new ArrayList<>();
        tipo.campos.forEach((nombre, estatico) -> { if (!estatico) atributos.add(nombre); });
        return atributos;
    }

    /** Cuerpo de a lo sumo una sentencia que solo lee o asigna un atributo: {@code return x;} / {@code this.x = x;}. */
    static boolean esAccesor(MetodoJava m, List<String> atributos) {
        List<Token> t = m.tokens;
        int abre = -1;
        for (int i = 0; i < t.size(); i++) if (t.get(i).es("{")) { abre = i; break; }
        if (abre < 0) return false;
        List<Token> cuerpo = t.subList(abre + 1, t.size() - 1);
        long sentencias = cuerpo.stream().filter(x -> x.es(";")).count();
        if (sentencias != 1 || cuerpo.size() > 6) return false;
        long atributosUsados = atributos.stream().filter(a -> cuerpo.stream().anyMatch(x -> x.es(a))).count();
        return atributosUsados == 1;
    }

    private static boolean usaAtributo(MetodoJava metodo, String atributo) {
        List<Token> t = metodo.tokens;
        for (int i = 0; i < t.size(); i++) {
            if (!t.get(i).es(atributo)) continue;
            boolean calificadoPorOtro = i > 0 && t.get(i - 1).es(".") && !(i > 1 && t.get(i - 2).es("this"));
            if (!calificadoPorOtro) return true;
        }
        return false;
    }
}
