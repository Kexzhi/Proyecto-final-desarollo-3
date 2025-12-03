package mx.unison.monitor;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

// Fuente de datos que lee del Arduino por puerto serie.
// Espera líneas con el formato: x:#,y:#,z:#
public class SerialDataSource implements SensorDataSource {

    // Puerto serie que usamos
    private final SerialPort port;
    // Lector de texto para leer líneas completas
    private final BufferedReader reader;

    // Construye la fuente de datos a partir del puerto encontrado
    public SerialDataSource(SerialPort port) throws Exception {
        this.port = port;

        // Configuración del puerto para que coincida con el sketch de Arduino
        // El código de Arduino usa Serial.begin(9600);
        port.setBaudRate(9600);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);

        // Configuro timeouts: espero hasta 1000 ms por datos
        // y si no llega nada, jSerialComm lanza SerialPortTimeoutException
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

        // Intento abrir el puerto
        if (!port.openPort()) {
            throw new Exception("No se pudo abrir el puerto serie " + port.getSystemPortName());
        }

        // Envolvemos el InputStream en un BufferedReader para poder usar readLine()
        this.reader = new BufferedReader(new InputStreamReader(port.getInputStream()));
    }

    // Lee la siguiente lectura enviada por el Arduino
    @Override
    public SensorData readNext() {
        try {
            // Espero una línea completa del Arduino (terminada en \n)
            String line = reader.readLine();

            // Si no llegó nada, no hago nada en esta vuelta
            if (line == null) {
                return null;
            }

            line = line.trim();
            if (line.isEmpty()) {
                return null;
            }

            // Formato esperado: x:#,y:#,z:#  (ej. "x:12,y:34,z:56")
            double x = 0;
            double y = 0;
            double z = 0;

            String[] parts = line.split(",");
            for (String part : parts) {
                String[] kv = part.split(":");
                if (kv.length != 2) {
                    // Si esa parte no tiene "clave:valor", la ignoro
                    continue;
                }

                String key = kv[0].trim().toLowerCase();
                double value = Double.parseDouble(kv[1].trim());

                // Asigno según la letra
                switch (key) {
                    case "x" -> x = value;
                    case "y" -> y = value;
                    case "z" -> z = value;
                    default -> {
                        // Cualquier otra cosa la ignoro
                    }
                }
            }

            // Uso la hora actual como timestamp
            LocalDateTime ts = LocalDateTime.now();
            return new SensorData(ts, x, y, z);

        } catch (SerialPortTimeoutException ex) {
            // IMPORTANTE:
            // Aquí solo significa que en este segundo no llegó ningún dato.
            // No lo tomo como error, solo regreso null y sigo.
            return null;
        } catch (IOException ex) {
            // Errores de E/S de verdad (puerto desconectado, etc.)
            ServerLogWindow.log("SerialDataSource: error de E/S en el puerto: " + ex.getMessage());
            return null;
        } catch (Exception ex) {
            // Cualquier error de formato lo registro y sigo
            ServerLogWindow.log("SerialDataSource: dato inválido: " + ex.getMessage());
            return null;
        }
    }

    // Cierra el puerto serie y libera recursos
    @Override
    public void close() {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException ignored) {
        }

        if (port != null && port.isOpen()) {
            port.closePort();
        }
    }
}
