package mx.unison.monitor;

import javax.swing.*;
import java.awt.*;

// Ventana para mostrar logs del "servidor" y del sistema
public class ServerLogWindow extends JFrame {

    // Singleton, solo una ventana de log
    private static ServerLogWindow instance;

    // Área de texto donde se imprimen los logs
    private final JTextArea logArea;

    // Constructor privado (uso getInstance para abrirla)
    private ServerLogWindow() {
        super("Terminal del servidor");

        // Creo área de texto y scroll
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logArea);

        // Agrego al frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Tamaño por defecto
        setSize(600, 400);
        setLocationRelativeTo(null);

        // Que se oculte al cerrar, no que termine el programa
        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    // Obtener la instancia única de la ventana
    public static synchronized ServerLogWindow getInstance() {
        if (instance == null) {
            instance = new ServerLogWindow();
        }
        return instance;
    }

    // Agregar un mensaje a la ventana de log
    public static void log(String message) {
        // Aseguro que esto corra en el hilo de Swing
        SwingUtilities.invokeLater(() -> {
            ServerLogWindow window = getInstance();
            // Agrego el texto con salto de línea
            window.logArea.append(message + "\n");
            // Hago scroll al final
            window.logArea.setCaretPosition(window.logArea.getDocument().getLength());
        });
    }
}
