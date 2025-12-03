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
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pantalla para ver el monitoreo en tiempo real.
 */
public class MonitorPanel extends JPanel {

    // Referencia a la app para poder regresar al inicio
    private final MainApp mainApp;
    // Gestor de puertos para detectar Arduino
    private final SerialManager serialManager;

    // Series de la gráfica (X, Y, Z)
    private final TimeSeries xSeries;
    private final TimeSeries ySeries;
    private final TimeSeries zSeries;

    // Gráfica principal
    private final JFreeChart chart;

    // Botón para iniciar/detener monitoreo
    private final JButton startStopButton;

    // Bandera que dice si se está monitoreando o no
    private final AtomicBoolean monitoring = new AtomicBoolean(false);

    // Hilo que lee datos y actualiza todo
    private Thread monitorThread;

    // Fuente de datos actual (simulador o serial)
    private SensorDataSource currentSource;

    // Constructor, aquí armo todo el panel
    public MonitorPanel(MainApp mainApp, SerialManager serialManager) {
        this.mainApp = mainApp;
        this.serialManager = serialManager;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== Header con escudo, título y botón volver =====
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel escudoLabel = new JLabel();
        escudoLabel.setIcon(ImageUtils.loadEscudo(60, 60)); // Escudo arriba a la izquierda

        JLabel titleLabel = new JLabel("Monitor en tiempo real");
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

        // Creo gráfica de serie de tiempo
        chart = ChartFactory.createTimeSeriesChart(
                "Datos en tiempo real (x, y, z)",
                "Tiempo",
                "Valor",
                dataset,
                true,
                true,
                false
        );

        // Rango de 0 a 100 para que coincida con el simulador/Arduino
        XYPlot plot = chart.getXYPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0.0, 100.0);

        // Panel que muestra la gráfica
        ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);

        // ===== Panel inferior: botón iniciar/detener =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);

        startStopButton = new JButton("Iniciar monitoreo");
        startStopButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        startStopButton.setPreferredSize(new Dimension(220, 45));
        // Parado = verde
        startStopButton.setBackground(Color.GREEN.darker());
        startStopButton.setForeground(Color.WHITE);
        startStopButton.setFocusPainted(false);

        startStopButton.addActionListener(e -> {
            // Si no estamos monitoreando, lo iniciamos
            if (!monitoring.get()) {
                startMonitoring();
            } else {
                // Si ya estaba, lo detenemos
                stopMonitoring();
            }
        });

        bottom.add(startStopButton);
        add(bottom, BorderLayout.SOUTH);

        // Aplico colores según tema
        updateTheme();
    }

    // Inicia el monitoreo (primero intento usar Arduino, luego simulador)
    private void startMonitoring() {
        monitoring.set(true);
        startStopButton.setText("Detener monitoreo");

        try {
            // Primero intento encontrar Arduino
            var port = serialManager.findArduinoPort();

            if (port != null) {
                // Si encuentro puerto, uso modo REAL
                currentSource = new SerialDataSource(port);
                ServerLogWindow.log("Monitor iniciado usando ARDUINO (modo REAL).");
                setRunningColor(true);   // Azul = Arduino
            } else {
                // Si no encuentro Arduino, uso simulador
                currentSource = new SimulatorDataSource();
                ServerLogWindow.log("No se detectó Arduino, monitor en modo SIMULADOR.");
                setRunningColor(false);  // Rojo = simulador
            }

        } catch (Exception ex) {
            // Si algo truena con el serial, me quedo en simulador
            ServerLogWindow.log("Error al iniciar con Arduino, uso SIMULADOR: " + ex.getMessage());
            currentSource = new SimulatorDataSource();
            setRunningColor(false);
        }

        // Creo el hilo que hará el loop de lectura
        monitorThread = new Thread(this::monitorLoop, "MonitorThread");
        monitorThread.start();
    }

    // Detiene el monitoreo (botón se pone verde y apago hilo)
    private void stopMonitoring() {
        monitoring.set(false);

        startStopButton.setText("Iniciar monitoreo");
        // Parado = verde
        SwingUtilities.invokeLater(() ->
                startStopButton.setBackground(Color.GREEN.darker()));

        // Cierro la fuente de datos
        if (currentSource != null) {
            currentSource.close();
            currentSource = null;
        }

        // Apago el hilo si seguía vivo
        if (monitorThread != null && monitorThread.isAlive()) {
            monitorThread.interrupt();
        }

        ServerLogWindow.log("Monitor detenido.");
    }

    // Cambiar color del botón dependiendo de la fuente
    // isSerial = true  -> Arduino (azul UNISON)
    // isSerial = false -> Simulador (rojo)
    private void setRunningColor(boolean isSerial) {
        SwingUtilities.invokeLater(() -> {
            if (isSerial) {
                // Arduino = azul UNISON
                startStopButton.setBackground(ThemeManager.getPrimaryColor());
            } else {
                // Simulador = rojo oscuro
                startStopButton.setBackground(Color.RED.darker());
            }
        });
    }

    // Loop principal del monitoreo, corre en un hilo aparte
    private void monitorLoop() {
        while (monitoring.get()) {
            try {
                // Reviso si tengo simulador y ya conectaron un Arduino
                ensureDataSource();

                // Si no tengo fuente (algo falló), espero un poco
                if (currentSource == null) {
                    Thread.sleep(1000);
                    continue;
                }

                // Pido un dato a la fuente (simulador o serial)
                SensorData data = currentSource.readNext();
                if (data != null) {
                    // Actualizo gráfica
                    addDataToChart(data);
                    // Envío al servidor para guardarlo en la BD
                    ServerClient.sendSensorData(data);
                }

            } catch (InterruptedException ex) {
                // Si interrumpen el hilo, termino el loop
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable t) {
                String msg = t.getMessage();
                // Si por alguna razón vuelve a salir el timeout, solo lo aviso
                if (msg != null && msg.contains("timed out before any data was returned")) {
                    ServerLogWindow.log("Aviso: timeout de lectura desde Arduino (no llegó dato a tiempo).");
                } else {
                    // Otros errores sí los registro
                    ServerLogWindow.log("Error en monitorLoop: " + msg);
                }
                // Ya NO cambio a simulador aquí. Me quedo con la fuente actual.
            }
        }
    }

    // Ver si ya hay un Arduino conectado y estoy en simulador para cambiar a REAL
    private void ensureDataSource() throws Exception {
        // Si ya no estoy monitoreando, no hago nada
        if (!monitoring.get()) return;

        // Solo intento cambiar si AHORA mismo estoy simulando
        if (!(currentSource instanceof SimulatorDataSource)) {
            return;
        }

        // Pregunto al SerialManager si ya vio un Arduino
        var port = serialManager.findArduinoPort();
        if (port != null) {
            // Cierro simulador y salto a Arduino real
            if (currentSource != null) {
                currentSource.close();
            }
            currentSource = new SerialDataSource(port);

            // Log grande para que se note en la “terminal”
            ServerLogWindow.log("****************************************************");
            ServerLogWindow.log("***   ARDUINO DETECTADO -> CAMBIO A MODO REAL    ***");
            ServerLogWindow.log("****************************************************");

            // Cambio color del botón a azul (Arduino)
            setRunningColor(true);
        }
    }

    // Agrega un nuevo punto (X,Y,Z) a la gráfica
    private void addDataToChart(SensorData data) {
        // Paso de LocalDateTime a Date para JFreeChart
        var instant = data.getTimestamp().atZone(ZoneId.systemDefault()).toInstant();
        java.util.Date date = java.util.Date.from(instant);
        Second second = new Second(date);

        // Actualizo las series en el hilo de la GUI
        SwingUtilities.invokeLater(() -> {
            xSeries.addOrUpdate(second, data.getX());
            ySeries.addOrUpdate(second, data.getY());
            zSeries.addOrUpdate(second, data.getZ());
        });
    }

    // Ajustar colores de la gráfica según tema claro/oscuro
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
        repaint();
    }
}
