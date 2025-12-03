package mx.unison.monitor;

/**
 * Tipos de filtro que se pueden aplicar en la vista Histórico.
 */
public enum HistoricalFilterType {
    ALL,
    BY_DAY,
    BY_HOUR,
    BY_MINUTE,
    LAST_5_MINUTES;

    @Override
    public String toString() {
        return switch (this) {
            case ALL -> "Todos los datos de la BD";
            case BY_DAY -> "Datos de ese día";
            case BY_HOUR -> "Datos de esa hora";
            case BY_MINUTE -> "Datos de ese minuto";
            case LAST_5_MINUTES -> "Datos de hace 5 minutos";
        };
    }
}
