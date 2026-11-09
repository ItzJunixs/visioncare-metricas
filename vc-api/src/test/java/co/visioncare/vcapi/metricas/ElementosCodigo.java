package co.visioncare.vcapi.metricas;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Modelo mínimo del código medido: tipos (clases, interfaces, enums, records) y sus métodos. */
public final class ElementosCodigo {

    private ElementosCodigo() {
    }

    /** Una clase, interfaz, enum o record encontrado en un archivo fuente. */
    public static final class TipoJava {
        public final String nombre;
        public final String paquete;
        public final String clase;      // class | interface | enum | record
        public final String archivo;
        public String superclase;        // nombre simple, o null si no tiene extends
        public final Set<String> interfaces = new LinkedHashSet<>();
        /** campo -> esEstatico */
        public final Map<String, Boolean> campos = new LinkedHashMap<>();
        public final List<MetodoJava> metodos = new ArrayList<>();
        /** Tokens propios del tipo (sin los de tipos anidados): base de CBO y RFC. */
        public final List<Token> tokens = new ArrayList<>();
        /** nombre simple -> nombre completo, tomado de los import del archivo. */
        public Map<String, String> imports = Map.of();

        public TipoJava(String nombre, String paquete, String clase, String archivo) {
            this.nombre = nombre;
            this.paquete = paquete;
            this.clase = clase;
            this.archivo = archivo;
        }

        /** Módulo del monolito: primer segmento del paquete después de co.visioncare.vcapi. */
        public String modulo() {
            String base = "co.visioncare.vcapi.";
            if (!paquete.startsWith(base)) return "(raiz)";
            String resto = paquete.substring(base.length());
            int punto = resto.indexOf('.');
            return punto < 0 ? resto : resto.substring(0, punto);
        }

        /** Capa dentro del módulo (controller, service, entity, repository, dto, config...) o "api". */
        public String capa() {
            String[] partes = paquete.split("\\.");
            String ultima = partes[partes.length - 1];
            if (partes.length <= 4) return "api/facade";          // co.visioncare.vcapi.<modulo>
            if (ultima.equals("internal")) return "internal";      // implementaciones de la fachada, listeners
            return ultima;
        }
    }

    /** Un método o constructor con su cuerpo tokenizado. */
    public static final class MetodoJava {
        public final String nombre;
        public final boolean constructor;
        public final boolean estatico;
        public final int parametros;
        public final List<Token> tokens;    // cabecera + cuerpo
        public final boolean tieneCuerpo;

        public MetodoJava(String nombre, boolean constructor, boolean estatico, int parametros,
                          List<Token> tokens, boolean tieneCuerpo) {
            this.nombre = nombre;
            this.constructor = constructor;
            this.estatico = estatico;
            this.parametros = parametros;
            this.tokens = tokens;
            this.tieneCuerpo = tieneCuerpo;
        }

        public int lineaInicio() { return tokens.isEmpty() ? 0 : tokens.get(0).linea(); }

        /** Líneas efectivas del método: líneas distintas que contienen algún token. */
        public int nloc() {
            return (int) tokens.stream().mapToInt(Token::linea).distinct().count();
        }
    }
}
