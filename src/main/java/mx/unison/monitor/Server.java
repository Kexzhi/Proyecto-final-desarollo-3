package mx.unison.monitor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

// Servidor que escucha en un puerto y guarda datos en la BD
public class Server {

    // Puerto donde escucha el servidor
    private final int port;
    // Hilo del servidor
    private Thread serverThread;
    // Bandera para saber si debe seguir corriendo
    private volatile boolean running = false;

    // Constructor, guardo el puerto
    public Server(int port) {
        this.port = port;
    }

    // Iniciar servidor en un hilo aparte
    public void start() {
        // Si ya hay un hilo corriendo, no hago nada
        if (serverThread != null && serverThread.isAlive()) {
            return;
        }

        running = true;

        // Creo y arranco el hilo del servidor
        serverThread = new Thread(this::runServer, "ServerThread");
        serverThread.start();
    }

    // Loop principal del servidor
    private void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Mensaje de que ya se levantó el servidor
            String msg = "Servidor iniciado en puerto " + port;
            System.out.println(msg);
            ServerLogWindow.log(msg);

            // Loop de aceptación de clientes
            while (running) {
                // Espero una conexión
                Socket clientSocket = serverSocket.accept();
                // Manejo el cliente en otro hilo
                new Thread(() -> handleClient(clientSocket), "ClientHandler").start();
            }
        } catch (IOException e) {
            // Error en el servidor
            ServerLogWindow.log("Error en el servidor: " + e.getMessage());
        }
    }

    // Manejar un cliente (solo soportamos comando INSERT)
    private void handleClient(Socket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

            // Leo una línea (ejemplo: INSERT epoch x y z)
            String line = in.readLine();
            if (line == null) {
                return;
            }

            // Separo la línea por espacios
            String[] parts = line.split(" ");
            if (parts.length == 5 && parts[0].equalsIgnoreCase("INSERT")) {
                // Parseo valores
                long epochMillis = Long.parseLong(parts[1]);
                double x = Double.parseDouble(parts[2]);
                double y = Double.parseDouble(parts[3]);
                double z = Double.parseDouble(parts[4]);

                // Convierto epoch a LocalDateTime
                LocalDateTime ts = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(epochMillis),
                        ZoneId.systemDefault()
                );

                // Creo el objeto dato
                SensorData data = new SensorData(ts, x, y, z);

                // Lo guardo en BD
                DatabaseManager.insertSensorData(data);

                // Respondo OK al cliente
                out.println("OK");
                // Lo escribo en la terminal del servidor
                ServerLogWindow.log("Servidor guardó dato: " + data);
            } else {
                // Comando que no reconozco
                out.println("ERROR formato de comando");
            }

        } catch (Exception e) {
            // Si algo sale mal con el cliente, lo registro
            ServerLogWindow.log("Error manejando cliente: " + e.getMessage());
        }
    }
}
