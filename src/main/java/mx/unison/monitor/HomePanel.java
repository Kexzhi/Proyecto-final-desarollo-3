package mx.unison.monitor;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla de inicio del sistema.
 */
public class HomePanel extends JPanel {

    // Referencia a la ventana principal
    private final MainApp mainApp;

    // Botón para cambiar tema (sol/luna)
    private final JToggleButton themeToggle;

    // Botones principales
    private final JButton monitorButton;   // Ir a monitor
    private final JButton historicoButton; // Ir a histórico
    private final JButton terminalButton;  // Ver terminal servidor

    // Constructor, aquí armo toda la vista
    public HomePanel(MainApp mainApp) {
        this.mainApp = mainApp;

        // Uso BorderLayout para acomodar el contenido
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ===== Panel superior: título + autor + botón modo oscuro =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Título grande
        JLabel titleLabel = new JLabel("Sistema de Monitoreo en Tiempo Real - UNISON");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Nombre del autor
        JLabel authorLabel = new JLabel("Autor: Zaid Montaño Martínez");
        authorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        authorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Panel para apilar título y autor
        JPanel titleContainer = new JPanel(new GridLayout(2, 1));
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel);
        titleContainer.add(authorLabel);

        topPanel.add(titleContainer, BorderLayout.CENTER);

        // Botón de tema (sol/luna)
        themeToggle = new JToggleButton("🌙"); // Empiezo mostrando luna
        themeToggle.setFocusPainted(false);
        themeToggle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        themeToggle.addActionListener(e -> {
            // Cambio el tema global
            ThemeManager.toggleTheme();
            // Le digo a la app que actualice todos los paneles
            mainApp.applyThemeToAll();
            // Actualizo el iconito (sol/luna)
            updateToggleIcon();
        });

        // Lo alineo a la derecha
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightTop.setOpaque(false);
        rightTop.add(themeToggle);

        topPanel.add(rightTop, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // ===== Panel central: logo del ojo =====
        JLabel logoLabel = new JLabel();
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Cargo el logo del ojo, si no existe pongo texto
        ImageIcon logoIcon = ImageUtils.loadLogoOjoBuho(260, 220);
        if (logoIcon != null) {
            logoLabel.setIcon(logoIcon);
        } else {
            logoLabel.setText("LOGO UNISON");
        }

        add(logoLabel, BorderLayout.CENTER);

        // ===== Panel inferior: botones =====
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new GridLayout(2, 1, 10, 10));

        // Subpanel para monitor e histórico
        JPanel mainButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        mainButtonsPanel.setOpaque(false);

        // Botón para abrir monitor en tiempo real
        monitorButton = new JButton("Monitor en tiempo real");
        monitorButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        monitorButton.setPreferredSize(new Dimension(240, 50));
        monitorButton.addActionListener(e -> mainApp.showMonitor());

        // Botón para abrir histórico
        historicoButton = new JButton("Histórico");
        historicoButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        historicoButton.setPreferredSize(new Dimension(240, 50));
        historicoButton.addActionListener(e -> mainApp.showHistorico());

        mainButtonsPanel.add(monitorButton);
        mainButtonsPanel.add(historicoButton);

        // Subpanel para el botón de terminal
        JPanel secondaryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        secondaryPanel.setOpaque(false);

        // Botón para mostrar la ventana de logs del servidor
        terminalButton = new JButton("Ver terminal del servidor");
        terminalButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        terminalButton.addActionListener(e -> {
            // Pido la instancia de la ventana y la muestro
            ServerLogWindow window = ServerLogWindow.getInstance();
            window.setVisible(true);
        });

        secondaryPanel.add(terminalButton);

        bottomPanel.add(mainButtonsPanel);
        bottomPanel.add(secondaryPanel);

        add(bottomPanel, BorderLayout.SOUTH);

        // Ajusto iconito de sol/luna según el tema actual
        updateToggleIcon();
        // Aplico colores a los botones según el tema
        updateTheme();
    }

    // Actualizar colores cuando cambia el tema (lo llama MainApp)
    public void updateTheme() {
        // Fondo principal del panel
        setBackground(ThemeManager.getBackgroundColor());
        setOpaque(true);

        // Si es modo oscuro, uso colores UNISON
        if (ThemeManager.getCurrentTheme() == Theme.DARK) {
            // Monitor = azul UNISON
            styleButton(monitorButton, ThemeManager.getPrimaryColor(), Color.WHITE);
            // Histórico = dorado UNISON
            styleButton(historicoButton, ThemeManager.getSecondaryColor(), Color.WHITE);
            // Terminal = gris
            styleButton(terminalButton, ThemeManager.getAccentGray(), Color.BLACK);
        } else {
            // Modo claro: todos gris suave
            Color bg = ThemeManager.getAccentGray();
            Color fg = ThemeManager.getTextColor();
            styleButton(monitorButton, bg, fg);
            styleButton(historicoButton, bg, fg);
            styleButton(terminalButton, bg, fg);
        }

        repaint();
    }

    // Darle estilo base a un botón (fondo, texto, padding)
    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setOpaque(true);
    }

    // Cambiar icono del toggle dependiendo del tema
    private void updateToggleIcon() {
        if (ThemeManager.getCurrentTheme() == Theme.DARK) {
            // Si estoy en oscuro, muestro sol (para volver a claro)
            themeToggle.setText("☀");
        } else {
            // Si estoy en claro, muestro luna (para ir a oscuro)
            themeToggle.setText("🌙");
        }
    }
}
