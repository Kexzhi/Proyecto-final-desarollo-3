package mx.unison.monitor;

import javax.swing.*;

// Clase principal de la aplicación (aquí inicia todo)
public class MainApp {

    // Ventana principal
    private JFrame frame;

    // Panels principales
    private HomePanel homePanel;
    private MonitorPanel monitorPanel;
    private HistoricoPanel historicoPanel;

    // Manejo de puertos serie
    private SerialManager serialManager;

    // Servidor interno
    private Server server;

    // Método main, punto de entrada del programa
    public static void main(String[] args) {
        // Lanzo la app en el hilo de Swing
        SwingUtilities.invokeLater(() -> {
            MainApp app = new MainApp();
            app.init();
        });
    }

    // Inicializar todo (BD, servidor, UI)
    private void init() {
        // Inicializar BD (por si acaso)
        DatabaseManager.initDatabase();

        // Crear y arrancar el servidor interno
        server = new Server(5555);
        server.start();

        // Crear el frame principal
        frame = new JFrame("Sistema de Monitoreo en Tiempo Real - UNISON");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crear serialManager
        serialManager = new SerialManager();

        // Crear los paneles y pasar referencia a esta MainApp
        homePanel = new HomePanel(this);
        monitorPanel = new MonitorPanel(this, serialManager);
        historicoPanel = new HistoricoPanel(this);

        // Aplicar tema a todos por si ya cambió
        applyThemeToAll();

        // Mostrar pantalla de inicio
        frame.setContentPane(homePanel);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Mostrar pantalla de inicio
    public void showHome() {
        frame.setContentPane(homePanel);
        refreshFrame();
    }

    // Mostrar monitor tiempo real
    public void showMonitor() {
        frame.setContentPane(monitorPanel);
        refreshFrame();
    }

    // Mostrar histórico
    public void showHistorico() {
        frame.setContentPane(historicoPanel);
        refreshFrame();
    }

    // Refrescar el frame después de cambiar panel
    private void refreshFrame() {
        frame.revalidate();
        frame.repaint();
    }

    // Aplicar tema a todos los paneles
    public void applyThemeToAll() {
        if (homePanel != null) {
            homePanel.updateTheme();
        }
        if (monitorPanel != null) {
            monitorPanel.updateTheme();
        }
        if (historicoPanel != null) {
            historicoPanel.updateTheme();
        }
    }
}
