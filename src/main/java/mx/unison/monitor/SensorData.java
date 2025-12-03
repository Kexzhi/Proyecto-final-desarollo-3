package mx.unison.monitor;

import java.time.LocalDateTime;

// Representa un dato del sensor (x, y, z y su tiempo)
public class SensorData {

    // Momento en que se tomó el dato
    private final LocalDateTime timestamp;
    // Valores numéricos
    private final double x;
    private final double y;
    private final double z;

    // Constructor con todos los campos
    public SensorData(LocalDateTime timestamp, double x, double y, double z) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Regresar fecha y hora
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Regresar valor X
    public double getX() {
        return x;
    }

    // Regresar valor Y
    public double getY() {
        return y;
    }

    // Regresar valor Z
    public double getZ() {
        return z;
    }

    // Para imprimir el dato en logs
    @Override
    public String toString() {
        return "SensorData{" +
                "timestamp=" + timestamp +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
