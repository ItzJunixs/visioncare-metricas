package co.visioncare.vcapi.metricas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.visioncare.vcapi.metricas.ElementosCodigo.MetodoJava;
import co.visioncare.vcapi.metricas.ElementosCodigo.TipoJava;

/**
 * Recorre los tokens de un archivo y reconoce tipos, campos y métodos (incluidos tipos anidados,
 * records con constructor compacto y enums con constantes). No construye un AST completo: solo
 * la estructura necesaria para McCabe, LOC, Halstead y la suite CK.
 */
public final class AnalizadorEstructura {

    private static final Set<String> TIPOS = Set.of("class", "interface", "enum", "record");
    private static final Set<String> MODIFICADORES = Set.of("public", "private", "protected", "static",
            "final", "abstract", "synchronized", "native", "transient", "volatile", "default", "strictfp",
            "sealed", "non-sealed");

    private final List<Token> t;
    private final String archivo;
    private String paquete = "";
    private final Map<String, String> imports = new HashMap<>();
    private final List<TipoJava> tipos = new ArrayList<>();

    private AnalizadorEstructura(List<Token> tokens, String archivo) {
        this.t = tokens;
        this.archivo = archivo;
    }

    public static List<TipoJava> analizar(List<Token> tokens, String archivo) {
        AnalizadorEstructura a = new AnalizadorEstructura(tokens, archivo);
        a.unidadCompilacion();
        return a.tipos;
    }

    private void unidadCompilacion() {
        int i = 0;
        while (i < t.size()) {
            if (t.get(i).es("package")) {
                StringBuilder sb = new StringBuilder();
                i++;
                while (!t.get(i).es(";")) sb.append(t.get(i++).texto());
                paquete = sb.toString();
                i++;
            } else if (t.get(i).es("import")) {
                StringBuilder sb = new StringBuilder();
                i++;
                if (t.get(i).es("static")) i++;
                while (!t.get(i).es(";")) sb.append(t.get(i++).texto());
                String fqcn = sb.toString();
                imports.put(fqcn.substring(fqcn.lastIndexOf('.') + 1), fqcn);
                i++;
            } else if (t.get(i).es("@") && i + 1 < t.size() && !t.get(i + 1).es("interface")) {
                i = saltarAnotacion(i);
            } else if (esInicioDeTipo(i)) {
                i = tipo(i, null);
            } else {
                i++;
            }
        }
    }

    private boolean esInicioDeTipo(int i) {
        if (i + 1 >= t.size()) return false;
        if (!TIPOS.contains(t.get(i).texto())) return false;
        if (t.get(i + 1).tipo() != Token.Tipo.IDENTIFICADOR) return false;
        return i == 0 || !t.get(i - 1).es(".");
    }

    /** Analiza un tipo que empieza en la palabra clave {@code class/interface/enum/record}. */
    private int tipo(int kw, TipoJava externo) {
        String clase = t.get(kw).texto();
        if (kw > 0 && t.get(kw - 1).es("@")) clase = "interface"; // @interface
        String nombre = t.get(kw + 1).texto();
        TipoJava tipo = new TipoJava(nombre, paquete, clase, archivo);
        tipo.imports = imports;
        tipos.add(tipo);

        int i = kw + 2;
        // Cabecera: genéricos, componentes del record, extends, implements
        while (!t.get(i).es("{")) {
            Token tk = t.get(i);
            tipo.tokens.add(tk);
            if (tk.es("(") && clase.equals("record")) {
                int cierre = cierreDe(i, "(", ")");
                componentesDeRecord(i + 1, cierre, tipo);
                for (int k = i + 1; k <= cierre; k++) tipo.tokens.add(t.get(k));
                i = cierre + 1;
                continue;
            }
            if (tk.es("extends") && clase.equals("class")) {
                tipo.superclase = t.get(i + 1).texto();
            }
            if (tk.es("implements") || (tk.es("extends") && clase.equals("interface"))) {
                int k = i + 1;
                while (!t.get(k).es("{")) {
                    if (t.get(k).tipo() == Token.Tipo.IDENTIFICADOR && (t.get(k - 1).es(",") || k == i + 1)) {
                        tipo.interfaces.add(t.get(k).texto());
                    }
                    k++;
                }
            }
            i++;
        }
        int finCuerpo = cierreDe(i, "{", "}");
        cuerpo(i + 1, finCuerpo, tipo);
        return finCuerpo + 1;
    }

    private void componentesDeRecord(int desde, int hasta, TipoJava tipo) {
        int nivel = 0;
        for (int k = desde; k < hasta; k++) {
            Token tk = t.get(k);
            if (tk.es("<") || tk.es("(")) nivel++;
            if (tk.es(">") || tk.es(")")) nivel--;
            boolean finDeComponente = k + 1 == hasta || (nivel == 0 && t.get(k + 1).es(","));
            if (finDeComponente && tk.tipo() == Token.Tipo.IDENTIFICADOR) {
                tipo.campos.put(tk.texto(), false);
            }
        }
    }

    private void cuerpo(int desde, int hasta, TipoJava tipo) {
        int i = desde;
        if (tipo.clase.equals("enum")) {
            i = saltarConstantesEnum(desde, hasta, tipo);
        }
        while (i < hasta) {
            Token tk = t.get(i);
            if (tk.es(";")) { i++; continue; }
            if (tk.es("@") && !t.get(i + 1).es("interface")) { i = saltarAnotacion(i); continue; }
            if (tk.es("@") && t.get(i + 1).es("interface")) { i = tipo(i + 1, tipo); continue; }
            int sinModificadores = primerMiembroNoModificador(i);
            if (esInicioDeTipo(sinModificadores)) { i = tipo(sinModificadores, tipo); continue; }
            i = miembro(i, hasta, tipo);
        }
    }

    private int saltarConstantesEnum(int desde, int hasta, TipoJava tipo) {
        int nivel = 0;
        for (int k = desde; k < hasta; k++) {
            Token tk = t.get(k);
            tipo.tokens.add(tk);
            if (tk.es("(") || tk.es("{")) nivel++;
            if (tk.es(")") || tk.es("}")) nivel--;
            if (nivel == 0 && tk.es(";")) return k + 1;
        }
        return hasta;
    }

    /** Reconoce un campo, un método, un constructor o un bloque inicializador. */
    private int miembro(int inicio, int hasta, TipoJava tipo) {
        int nivelParentesis = 0;
        int primerParentesis = -1;
        boolean estatico = false;
        int inicioDeclaracion = inicio; // primer token después de las anotaciones del miembro
        int k = inicio;
        while (k < hasta) {
            Token tk = t.get(k);
            if (nivelParentesis == 0 && tk.es("static")) estatico = true;
            if (tk.es("@") && nivelParentesis == 0) {
                k = saltarAnotacion(k);
                if (primerParentesis < 0) inicioDeclaracion = k;
                continue;
            }
            if (tk.es("(")) { if (nivelParentesis == 0 && primerParentesis < 0) primerParentesis = k; nivelParentesis++; }
            if (tk.es(")")) nivelParentesis--;
            if (nivelParentesis == 0) {
                if (tk.es("=") && primerParentesis < 0) {
                    // Campo con inicializador: el nombre es el identificador antes del '='
                    tipo.campos.put(t.get(k - 1).texto(), estatico);
                    int fin = finDeSentencia(k);
                    agregarTokens(tipo, inicio, fin);
                    return fin + 1;
                }
                if (tk.es(";")) {
                    agregarTokens(tipo, inicio, k);
                    if (primerParentesis >= 0) { // método abstracto o de interfaz
                        tipo.metodos.add(new MetodoJava(t.get(primerParentesis - 1).texto(), false, estatico,
                                contarParametros(primerParentesis), sublista(inicioDeclaracion, k), false));
                    } else {
                        for (int c = inicio; c < k; c++) { // int a, b;  -> ambos campos
                            if ((t.get(c + 1).es(",") || c + 1 == k) && t.get(c).tipo() == Token.Tipo.IDENTIFICADOR) {
                                tipo.campos.put(t.get(c).texto(), estatico);
                            }
                        }
                    }
                    return k + 1;
                }
                if (tk.es("{")) {
                    int fin = cierreDe(k, "{", "}");
                    agregarTokens(tipo, inicio, fin);
                    String nombre = primerParentesis >= 0 ? t.get(primerParentesis - 1).texto() : t.get(k - 1).texto();
                    boolean esCompacto = primerParentesis < 0 && nombre.equals(tipo.nombre);
                    if (primerParentesis >= 0 || esCompacto) {
                        boolean constructor = nombre.equals(tipo.nombre);
                        int params = esCompacto ? 0 : contarParametros(primerParentesis);
                        tipo.metodos.add(new MetodoJava(nombre, constructor, estatico, params,
                                sublista(primerMiembroNoModificador(inicioDeclaracion), fin), true));
                    }
                    // si no, es un bloque inicializador {...} o static {...}: sus tokens ya cuentan para la clase
                    return fin + 1;
                }
            }
            k++;
        }
        return hasta;
    }

    private int primerMiembroNoModificador(int i) {
        while (MODIFICADORES.contains(t.get(i).texto())) i++;
        return i;
    }

    private int finDeSentencia(int desde) {
        int nivel = 0;
        for (int k = desde; k < t.size(); k++) {
            Token tk = t.get(k);
            if (tk.es("(") || tk.es("{") || tk.es("[")) nivel++;
            if (tk.es(")") || tk.es("}") || tk.es("]")) nivel--;
            if (nivel == 0 && tk.es(";")) return k;
        }
        return t.size() - 1;
    }

    private int contarParametros(int abre) {
        int cierra = cierreDe(abre, "(", ")");
        if (cierra == abre + 1) return 0;
        int nivel = 0, comas = 0;
        for (int k = abre + 1; k < cierra; k++) {
            Token tk = t.get(k);
            if (tk.es("<") || tk.es("(")) nivel++;
            if (tk.es(">") || tk.es(")")) nivel--;
            if (nivel == 0 && tk.es(",")) comas++;
        }
        return comas + 1;
    }

    private int saltarAnotacion(int arroba) {
        int k = arroba + 2;
        while (k + 1 < t.size() && t.get(k).es(".")) k += 2;
        if (k < t.size() && t.get(k).es("(")) k = cierreDe(k, "(", ")") + 1;
        return k;
    }

    private int cierreDe(int abre, String a, String c) {
        int nivel = 0;
        for (int k = abre; k < t.size(); k++) {
            if (t.get(k).es(a)) nivel++;
            if (t.get(k).es(c) && --nivel == 0) return k;
        }
        throw new IllegalStateException("Sin cierre para '" + a + "' en " + archivo + ":" + t.get(abre).linea());
    }

    private void agregarTokens(TipoJava tipo, int desde, int hasta) {
        for (int k = desde; k <= hasta && k < t.size(); k++) tipo.tokens.add(t.get(k));
    }

    private List<Token> sublista(int desde, int hasta) {
        return new ArrayList<>(t.subList(desde, Math.min(hasta + 1, t.size())));
    }
}
