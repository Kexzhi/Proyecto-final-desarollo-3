package mx.unison.monitor;

import java.time.LocalDate;
import java.util.List;

// Clase de apoyo para las consultas, envuelve la BD
public class ServerCore {

    // Insertar un dato (por si se usa directo)
    public static void insertSensorData(SensorData data) {
        DatabaseManager.insertSensorData(data);
    }

    // Regresar todos los datos
    public static List<SensorData> getAllData() {
        return DatabaseManager.getAllData();
    }

    // Regresar datos de un día específico
    public static List<SensorData> getDataByDay(LocalDate date) {
        return DatabaseManager.getDataByDay(date);
    }

    // Regresar datos de una hora específica de un día
    public static List<SensorData> getDataByHour(LocalDate date, int hour) {
        return DatabaseManager.getDataByHour(date, hour);
    }

    // Regresar datos de un minuto específico
    public static List<SensorData> getDataByMinute(LocalDate date, int hour, int minute) {
        return DatabaseManager.getDataByMinute(date, hour, minute);
    }

    // Regresar datos de los últimos X minutos (el método que te marcaba error)
    public static List<SensorData> getDataLastMinutes(int minutes) {
        return DatabaseManager.getLastMinutes(minutes);
    }
}
