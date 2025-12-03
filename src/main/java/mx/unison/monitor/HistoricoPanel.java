package mx.unison.monitor;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.Date;
import java.util.List;

/**
 * Pantalla del histórico de datos.
 */
public class HistoricoPanel extends JPanel {

    // Referencia a la app para regresar al inicio
    private final MainApp mainApp;

    // Series de la gráfica
    private final TimeSeries xSeries;
    private final TimeSeries ySeries;
    private final TimeSeries zSeries;

    // Gráfica principal
    private final JFreeChart chart;

    // Controles para filtro por fecha/hora exacta
    private final JSpinner exactDateSpinner;              // Fecha
    private final JSpinner exactTimeSpinner;              // Hora
    private final JComboBox<HistoricalExactLevel> exactLevelCombo; // Día / Hora / Minuto
    private final JButton exactQueryButton;               // Botón consultar

    // Controles para filtros rápidos
    private final JComboBox<HistoricalQuickFilter> quickFilterCombo; // Tipo de filtro rápido
    private final JButton quickQueryButton;                           // Botón consultar rápido

    // Etiqueta para mostrar mensajes (cuántos registros, errores, etc.)
    private final JLabel statusLabel;

    // Constructor, aquí armo la vista
    public HistoricoPanel(MainApp mainApp) {
        this.mainApp = mainApp;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== Header con escudo, título y botón volver =====
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel escudoLabel = new JLabel();
        escudoLabel.setIcon(ImageUtils.loadEscudo(60, 60));

        JLabel titleLabel = new JLabel("Histórico de datos");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton backButton = new JButton("Volver al inicio");
        backButton.addActionListener(e -> mainApp.showHome());

        header.add(escudoLabel, BorderLayout.WEST);
        header.add(titleLabel, BorderLayout.CENTER);
        header.add(backButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ===== Configuración de la gráfica =====
        xSeries = new TimeSeries("X");
        ySeries = new TimeSeries("Y");
        zSeries = new TimeSeries("Z");

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(xSeries);
        dataset.addSeries(ySeries);
        dataset.addSeries(zSeries);

        chart = ChartFactory.createTimeSeriesChart(
                "Datos históricos (x, y, z)",
                "Tiempo",
                "Valor",
                dataset,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0.0, 100.0);

        ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);

        // ===== Parte de abajo: filtros exactos + rápidos =====
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        // --- Panel izquierdo: filtro por fecha/hora exacta ---
        JPanel exactPanel = new JPanel();
        exactPanel.setOpaque(false);
        exactPanel.setBorder(BorderFactory.createTitledBorder("Filtrar por fecha/hora exacta"));
        exactPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        // Spinner de fecha (formato yyyy-MM-dd)
        exactDateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(exactDateSpinner, "yyyy-MM-dd");
        exactDateSpinner.setEditor(dateEditor);

        // Spinner de hora (formato HH:mm)
        exactTimeSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE));
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(exactTimeSpinner, "HH:mm");
        exactTimeSpinner.setEditor(timeEditor);

        // Combo para elegir si el filtro será por día, hora o minuto
        exactLevelCombo = new JComboBox<>(HistoricalExactLevel.values());

        // Botón para ejecutar la consulta exacta
        exactQueryButton = new JButton("Consultar por fecha/hora");
        exactQueryButton.addActionListener(e -> runExactQuery());

        // Coloco los componentes en el panel exacto
        c.gridx = 0; c.gridy = 0;
        exactPanel.add(new JLabel("Fecha:"), c);
        c.gridx = 1;
        exactPanel.add(exactDateSpinner, c);

        c.gridx = 0; c.gridy = 1;
        exactPanel.add(new JLabel("Hora:"), c);
        c.gridx = 1;
        exactPanel.add(exactTimeSpinner, c);

        c.gridx = 0; c.gridy = 2;
        exactPanel.add(new JLabel("Nivel:"), c);
        c.gridx = 1;
        exactPanel.add(exactLevelCombo, c);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        exactPanel.add(exactQueryButton, c);

        // --- Panel derecho: filtros rápidos ---
        JPanel quickPanel = new JPanel();
        quickPanel.setOpaque(false);
        quickPanel.setBorder(BorderFactory.createTitledBorder("Filtros rápidos (basados en ahora)"));
        quickPanel.setLayout(new GridBagLayout());
        GridBagConstraints q = new GridBagConstraints();
        q.insets = new Insets(4, 4, 4, 4);
        q.anchor = GridBagConstraints.WEST;

        // Combo con opciones de filtro rápido
        quickFilterCombo = new JComboBox<>(HistoricalQuickFilter.values());

        // Botón para ejecutar filtro rápido
        quickQueryButton = new JButton("Consultar filtro rápido");
        quickQueryButton.addActionListener(e -> runQuickQuery());

        q.gridx = 0; q.gridy = 0;
        quickPanel.add(new JLabel("Opción:"), q);
        q.gridx = 1;
        quickPanel.add(quickFilterCombo, q);

        q.gridx = 0; q.gridy = 1; q.gridwidth = 2;
        q.anchor = GridBagConstraints.CENTER;
        quickPanel.add(quickQueryButton, q);

        // Panel para juntar exactPanel y quickPanel
        JPanel filtersContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        filtersContainer.setOpaque(false);
        filtersContainer.add(exactPanel);
        filtersContainer.add(quickPanel);

        bottom.add(filtersContainer, BorderLayout.CENTER);

        // Etiqueta de estado (registros cargados, errores, etc.)
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        bottom.add(statusLabel, BorderLayout.SOUTH);

        add(bottom, BorderLayout.SOUTH);

        // Aplico colores según tema
        updateTheme();
    }

    // -------- Lógica de consultas --------

    // Consulta por fecha/hora exacta (día, hora o minuto)
    private void runExactQuery() {
        statusLabel.setText("Cargando datos por fecha/hora exacta...");
        statusLabel.setForeground(ThemeManager.getTextColor());

        // Uso un hilo para no congelar la interfaz
        new Thread(() -> {
            try {
                HistoricalExactLevel level = (HistoricalExactLevel) exactLevelCombo.getSelectedItem();
                if (level == null) return;

                // Tomo fecha del spinner
                Date dateValue = (Date) exactDateSpinner.getValue();
                Date timeValue = (Date) exactTimeSpinner.getValue();

                // Paso a LocalDate y LocalTime
                LocalDate localDate = dateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalTime localTime = timeValue.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

                List<SensorData> dataList;

                // Dependiendo del nivel, llamo a una consulta distinta
                switch (level) {
                    case DAY -> dataList = ServerClient.queryByDay(localDate);
                    case HOUR -> dataList = ServerClient.queryByHour(localDate, localTime.getHour());
                    case MINUTE -> dataList = ServerClient.queryByMinute(localDate, localTime.getHour(), localTime.getMinute());
                    default -> dataList = List.of();
                }

                // Actualizo gráfica con los datos
                updateChartData(dataList);
                int size = dataList.size();
                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("Consulta exacta: " + size + " registros."));

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("Error en consulta exacta: " + e.getMessage()));
            }
        }, "HistoricoExactQueryThread").start();
    }

    // Consulta con filtros rápidos (hoy, hora actual, últimos 5 min, etc.)
    private void runQuickQuery() {
        statusLabel.setText("Cargando datos con filtro rápido...");
        statusLabel.setForeground(ThemeManager.getTextColor());

        new Thread(() -> {
            try {
                HistoricalQuickFilter filter = (HistoricalQuickFilter) quickFilterCombo.getSelectedItem();
                if (filter == null) return;

                List<SensorData> dataList;
                LocalDateTime now = LocalDateTime.now();
                LocalDate today = now.toLocalDate();

                // Llamo a la consulta según la opción elegida
                switch (filter) {
                    case ALL_DB -> dataList = ServerClient.queryAll();
                    case TODAY -> dataList = ServerClient.queryByDay(today);
                    case CURRENT_HOUR -> dataList = ServerClient.queryByHour(today, now.getHour());
                    case CURRENT_MINUTE -> dataList = ServerClient.queryByMinute(today, now.getHour(), now.getMinute());
                    case LAST_5_MINUTES -> dataList = ServerClient.queryLastMinutes(5);
                    default -> dataList = List.of();
                }

                // Actualizo gráfica con los datos
                updateChartData(dataList);
                int size = dataList.size();
                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("Filtro rápido: " + size + " registros."));

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("Error en filtro rápido: " + e.getMessage()));
            }
        }, "HistoricoQuickQueryThread").start();
    }

    // Recargar la gráfica con una lista de datos nueva
    private void updateChartData(List<SensorData> dataList) {
        SwingUtilities.invokeLater(() -> {
            // Limpio las series
            xSeries.clear();
            ySeries.clear();
            zSeries.clear();

            // Agrego cada punto a la gráfica
            for (SensorData d : dataList) {
                var instant = d.getTimestamp().atZone(ZoneId.systemDefault()).toInstant();
                Date date = Date.from(instant);
                Second second = new Second(date);
                xSeries.addOrUpdate(second, d.getX());
                ySeries.addOrUpdate(second, d.getY());
                zSeries.addOrUpdate(second, d.getZ());
            }
        });
    }

    // Ajustar colores por tema
    public void updateTheme() {
        setBackground(ThemeManager.getBackgroundColor());

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(ThemeManager.getCardBackgroundColor());
        plot.setDomainGridlinePaint(ThemeManager.getAccentGray());
        plot.setRangeGridlinePaint(ThemeManager.getAccentGray());
        chart.setBackgroundPaint(ThemeManager.getBackgroundColor());

        if (chart.getTitle() != null) {
            chart.getTitle().setPaint(ThemeManager.getTextColor());
        }

        statusLabel.setForeground(ThemeManager.getTextColor());
        repaint();
    }
}
