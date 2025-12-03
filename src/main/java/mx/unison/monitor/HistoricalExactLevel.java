package mx.unison.monitor;

// Enum para el nivel de precisión del filtro exacto (día, hora, minuto)
public enum HistoricalExactLevel {
    DAY("Por día completo"),
    HOUR("Por hora específica"),
    MINUTE("Por minuto específico");

    // Texto que se mostrará en el combo
    private final String label;

    HistoricalExactLevel(String label) {
        this.label = label;
    }

    // Cuando el combo pinta el enum, usa este texto
    @Override
    public String toString() {
        return label;
    }
}
