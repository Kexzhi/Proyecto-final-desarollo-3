package mx.unison.monitor;

/**
 * Filtros rápidos, totalmente independientes de la fecha/hora elegida.
 *
 * Se basan en la fecha/hora actual del sistema.
 */
public enum HistoricalQuickFilter {
    ALL_DB("Todos los datos de la base"),
    TODAY("Datos del día actual"),
    CURRENT_HOUR("Datos de la hora actual"),
    CURRENT_MINUTE("Datos del minuto actual"),
    LAST_5_MINUTES("Datos de los últimos 5 minutos");

    private final String label;

    HistoricalQuickFilter(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
