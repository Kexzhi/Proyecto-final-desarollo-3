package mx.unison.monitor;

import com.fazecast.jSerialComm.SerialPort;

// Se encarga de elegir en qué puerto está el Arduino (CH340)
public class SerialManager {

    // Busca el puerto donde podría estar el Arduino
    public SerialPort findArduinoPort() {
        try {
            // Pido todos los puertos serie disponibles
            SerialPort[] ports = SerialPort.getCommPorts();

            // Si no hay puertos, no puedo hacer nada
            if (ports.length == 0) {
                ServerLogWindow.log("SerialManager: no hay puertos serie, uso simulador.");
                return null;
            }

            // Imprimo lo que ve la librería (para debug)
            ServerLogWindow.log("SerialManager: puertos detectados:");
            for (SerialPort p : ports) {
                ServerLogWindow.log(" - " + p.getSystemPortName() + " | " + p.getDescriptivePortName());
            }

            // 1) Intento encontrar algo que huela a CH340 o Arduino
            SerialPort elegido = null;
            for (SerialPort p : ports) {
                String desc = p.getDescriptivePortName().toLowerCase();
                String name = p.getSystemPortName().toLowerCase();

                // Aquí meto CH340 explícito porque es tu caso
                if (desc.contains("ch340") ||
                        desc.contains("arduino") ||
                        name.contains("ch340")) {

                    elegido = p;
                    break;
                }
            }

            // 2) Si no encontré CH340/Arduino, tomo el último puerto
            if (elegido == null) {
                elegido = ports[ports.length - 1];
                ServerLogWindow.log("SerialManager: no vi CH340/Arduino, uso el último puerto de la lista.");
            }

            // Mensaje claro de cuál puerto vamos a usar
            ServerLogWindow.log("SerialManager: usaré el puerto " +
                    elegido.getSystemPortName() + " (" + elegido.getDescriptivePortName() + ")");

            return elegido;

        } catch (Throwable t) {
            // Si jSerialComm truena (DLL, permisos, etc.), ya no puedo usar Arduino
            ServerLogWindow.log("SerialManager: error serio con jSerialComm, me quedo SOLO con simulador.");
            ServerLogWindow.log("Detalle técnico: " + t.toString());
            return null;
        }
    }
}
