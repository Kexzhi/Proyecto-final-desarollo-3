package mx.unison.monitor;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

// Cliente que habla con el servidor y/o con la BD
public class ServerClient {

    // Host y puerto del servidor
    private static final String HOST = "localhost";
    private static final int PORT = 5555;

    // Enviar un dato al servidor (INSERT)
    public static void sendSensorData(SensorData data) {
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Convierto timestamp a epoch millis
            long epoch = data.getTimestamp()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            // Armo comando: INSERT epoch x y z
            String command = "INSERT " + epoch + " " + data.getX() + " " + data.getY() + " " + data.getZ();
            out.println(command);

            // Leo respuesta (espero "OK")
            String resp = in.readLine();
            if (resp == null || !resp.startsWith("OK")) {
                ServerLogWindow.log("Respuesta inesperada del servidor: " + resp);
            }

        } catch (Exception e) {
            // Si no se puede conectar al servidor, guardo directo en BD
            ServerLogWindow.log("No se pudo enviar al servidor, guardo directo en BD: " + e.getMessage());
            DatabaseManager.insertSensorData(data);
        }
    }

    // Consultar TODO el histórico (usa directamente la BD)
    public static List<SensorData> queryAll() {
        return DatabaseManager.getAllData();
    }

    // Consultar por día específico
    public static List<SensorData> queryByDay(LocalDate date) {
        return DatabaseManager.getDataByDay(date);
    }

    // Consultar por hora específica de un día
    public static List<SensorData> queryByHour(LocalDate date, int hour) {
        return DatabaseManager.getDataByHour(date, hour);
    }

    // Consultar por minuto específico
    public static List<SensorData> queryByMinute(LocalDate date, int hour, int minute) {
        return DatabaseManager.getDataByMinute(date, hour, minute);
    }

    // Consultar últimos X minutos
    public static List<SensorData> queryLastMinutes(int minutes) {
        return DatabaseManager.getLastMinutes(minutes);
    }
}
