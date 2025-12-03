package mx.unison.monitor;

import java.time.LocalDateTime;
import java.util.Random;

// Genera datos aleatorios como si fuera el Arduino
public class SimulatorDataSource implements SensorDataSource {

    // Random para los valores
    private final Random random = new Random();

    // Constructor
    public SimulatorDataSource() {
        // Nada especial de inicio
    }

    // Regresar un dato nuevo cada vez que lo llamen
    @Override
    public SensorData readNext() throws Exception {
        // Espero 1 segundo para simular el delay del Arduino
        Thread.sleep(1000);

        // Genero valores entre 0 y 100
        double x = random.nextInt(101);
        double y = random.nextInt(101);
        double z = random.nextInt(101);

        // Uso la hora actual como timestamp
        LocalDateTime now = LocalDateTime.now();

        // Regreso el dato armado
        return new SensorData(now, x, y, z);
    }

    // Nada que cerrar aquí, pero lo dejo por la interfaz
    @Override
    public void close() {
        // Simulador no abre recursos, así que no cierro nada
    }
}
