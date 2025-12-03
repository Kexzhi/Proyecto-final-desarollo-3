package mx.unison.monitor;

/**
 * Interfaz que representa una fuente de datos del sensor.
 *
 * Puede ser:
 *  - Arduino real (SerialDataSource)
 *  - Simulador (SimulatorDataSource)
 */
public interface SensorDataSource {
    SensorData readNext() throws Exception;
    void close();
}
