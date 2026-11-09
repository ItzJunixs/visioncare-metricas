package co.visioncare.vcapi.metricas;

/**
 * Resultado de medir una clase con la suite CK (sección 3 del módulo), más el tamaño y el
 * Índice de Mantenibilidad de la clase para poder cruzar las métricas.
 *
 * @param cbo         acoplamiento con tipos no pertenecientes a {@code java.*} (criterio de la herramienta ck de Aniche)
 * @param cboProyecto acoplamiento solo con tipos del propio proyecto (co.visioncare.*)
 * @param lcom        LCOM* de Henderson-Sellers acotado a [0, 1] (0 = cohesiva, 1 = nada cohesiva)
 * @param lcom4       grupos de métodos conectados (sin accesores): 1 = cohesiva, 2+ = responsabilidades separables
 * @param mi          Índice de Mantenibilidad de la clase (NaN si no tiene métodos con cuerpo)
 */
public record ClaseCK(String nombre, String paquete, String modulo, String capa, String clase,
                      int wmc, int dit, int noc, int cbo, int cboProyecto, int rfc, double lcom, int lcom4,
                      int metodos, int campos, int nloc, double mi) {

    public static final int UMBRAL_WMC = 20;
    public static final int UMBRAL_DIT = 5;
    public static final int UMBRAL_CBO = 10;
    public static final int UMBRAL_RFC = 50;
    public static final double UMBRAL_LCOM = 0.7;

    public boolean superaWmc() { return wmc > UMBRAL_WMC; }
    public boolean superaDit() { return dit > UMBRAL_DIT; }
    public boolean superaCbo() { return cbo > UMBRAL_CBO; }
    public boolean superaRfc() { return rfc > UMBRAL_RFC; }
    /**
     * LCOM* supera 0.7 Y LCOM4 confirma al menos dos grupos independientes. Solo con LCOM* las
     * entidades con accesores saldrían como falsos positivos (ver {@link SuiteCK#lcom4}).
     */
    public boolean superaLcom() { return lcom > UMBRAL_LCOM && lcom4 > 1; }

    /** Igual que en el ejemplo de clase: WMC > 20 o CBO > 10 o LCOM > 0.7 (se agregan DIT y RFC). */
    public boolean esClaseProblematica() {
        return superaWmc() || superaDit() || superaCbo() || superaRfc() || superaLcom();
    }

    /** Clase Dios (sección 3.3): WMC alto, CBO alto y LCOM cercano a 1 a la vez. */
    public boolean esGodClass() {
        return superaWmc() && superaCbo() && superaLcom();
    }
}
