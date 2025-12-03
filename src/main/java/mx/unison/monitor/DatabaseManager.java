package mx.unison.monitor;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Maneja la conexión y consultas a la base de datos SQLite
public class DatabaseManager {

    // Nombre del archivo de la BD
    private static final String DB_URL = "jdbc:sqlite:monitorBD.db";

    // Se ejecuta una vez cuando se carga la clase
    static {
        initDatabase();
    }

    // Crear tabla si no existe y arreglar si está vieja (sin "timestamp")
    public static void initDatabase() {
        // SQL para crear la tabla de datos del sensor (esquema correcto)
        String createSql = """
                CREATE TABLE IF NOT EXISTS datos_sensor (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TEXT NOT NULL,
                    x REAL NOT NULL,
                    y REAL NOT NULL,
                    z REAL NOT NULL
                )
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Primero veo si la tabla existe
            boolean tableExists = false;
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='datos_sensor'")) {
                if (rs.next()) {
                    tableExists = true;
                }
            }

            // Si existe, reviso sus columnas
            if (tableExists) {
                boolean hasTimestamp = false;
                try (ResultSet rsInfo = stmt.executeQuery("PRAGMA table_info(datos_sensor)")) {
                    while (rsInfo.next()) {
                        String colName = rsInfo.getString("name");
                        if ("timestamp".equalsIgnoreCase(colName)) {
                            hasTimestamp = true;
                            break;
                        }
                    }
                }

                // Si la tabla existe pero NO tiene columna timestamp, la borro y la creo de nuevo
                if (!hasTimestamp) {
                    ServerLogWindow.log("Tabla datos_sensor vieja detectada (sin timestamp). Se recrea la tabla.");
                    stmt.executeUpdate("DROP TABLE datos_sensor");
                    tableExists = false;
                }
            }

            // Si no existe (o la acabo de borrar), la creo con el esquema correcto
            if (!tableExists) {
                stmt.executeUpdate(createSql);
                System.out.println("Tabla datos_sensor creada con columna timestamp.");
                ServerLogWindow.log("Tabla datos_sensor creada con columna timestamp.");
            } else {
                // Si ya estaba bien, solo aviso
                System.out.println("Tabla datos_sensor verificada correctamente.");
                ServerLogWindow.log("Tabla datos_sensor verificada correctamente.");
            }

        } catch (SQLException e) {
            // Si algo falla lo muestro
            ServerLogWindow.log("Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Obtener una conexión a la BD
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Insertar un dato del sensor en la BD
    public static void insertSensorData(SensorData data) {
        String sql = "INSERT INTO datos_sensor(timestamp, x, y, z) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Guardamos timestamp como texto ISO (LocalDateTime.toString)
            ps.setString(1, data.getTimestamp().toString());
            ps.setDouble(2, data.getX());
            ps.setDouble(3, data.getY());
            ps.setDouble(4, data.getZ());
            ps.executeUpdate();

        } catch (SQLException e) {
            // Si falla la inserción, lo registro
            ServerLogWindow.log("Error al insertar dato en BD: " + e.getMessage());
        }
    }

    // Obtener todos los datos
    public static List<SensorData> getAllData() {
        String sql = "SELECT timestamp, x, y, z FROM datos_sensor ORDER BY timestamp ASC";
        return runQuery(sql, null);
    }

    // Obtener datos de un día específico
    public static List<SensorData> getDataByDay(LocalDate date) {
        // Uso date(timestamp) para comparar solo la fecha
        String sql = "SELECT timestamp, x, y, z FROM datos_sensor " +
                "WHERE date(timestamp) = ? ORDER BY timestamp ASC";
        List<Object> params = new ArrayList<>();
        params.add(date.toString());
        return runQuery(sql, params);
    }

    // Obtener datos de una hora específica de un día
    public static List<SensorData> getDataByHour(LocalDate date, int hour) {
        // Defino inicio y fin de esa hora
        LocalDateTime start = date.atTime(hour, 0);
        LocalDateTime end = start.plusHours(1);

        String sql = "SELECT timestamp, x, y, z FROM datos_sensor " +
                "WHERE timestamp >= ? AND timestamp < ? ORDER BY timestamp ASC";

        List<Object> params = new ArrayList<>();
        params.add(start.toString());
        params.add(end.toString());
        return runQuery(sql, params);
    }

    // Obtener datos de un minuto específico de una hora/día
    public static List<SensorData> getDataByMinute(LocalDate date, int hour, int minute) {
        LocalDateTime start = date.atTime(hour, minute);
        LocalDateTime end = start.plusMinutes(1);

        String sql = "SELECT timestamp, x, y, z FROM datos_sensor " +
                "WHERE timestamp >= ? AND timestamp < ? ORDER BY timestamp ASC";

        List<Object> params = new ArrayList<>();
        params.add(start.toString());
        params.add(end.toString());
        return runQuery(sql, params);
    }

    // Datos de los últimos X minutos desde ahora (nombre nuevo)
    public static List<SensorData> getLastMinutes(int minutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);

        String sql = "SELECT timestamp, x, y, z FROM datos_sensor " +
                "WHERE timestamp >= ? ORDER BY timestamp ASC";

        List<Object> params = new ArrayList<>();
        params.add(cutoff.toString());
        return runQuery(sql, params);
    }

    // Alias por si alguien llama al nombre viejo
    public static List<SensorData> getDataLastMinutes(int minutes) {
        return getLastMinutes(minutes);
    }

    // Método genérico para correr una consulta y regresar lista de SensorData
    private static List<SensorData> runQuery(String sql, List<Object> params) {
        List<SensorData> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Si hay parámetros, los pongo en el PreparedStatement
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Leo valores
                    String tsText = rs.getString("timestamp");
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");

                    // Convierto timestamp a LocalDateTime
                    LocalDateTime ts = LocalDateTime.parse(tsText);

                    // Agrego a la lista
                    list.add(new SensorData(ts, x, y, z));
                }
            }

        } catch (SQLException e) {
            // Logueo el error y regreso lo que haya (posiblemente lista vacía)
            ServerLogWindow.log("Error al ejecutar consulta: " + e.getMessage());
        }

        return list;
    }
}
