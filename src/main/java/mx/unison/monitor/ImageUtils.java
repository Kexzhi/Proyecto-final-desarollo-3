package mx.unison.monitor;

import javax.swing.*;
import java.awt.*;

/**
 * Clase para cargar imágenes desde resources.
 */
public class ImageUtils {

    // Ruta del logo del ojo del búho (en src/main/resources)
    private static final String LOGO_OJO_PATH = "/LOGO-OJO-BUHO-NEGRO-2048x1669.jpg";
    // Ruta del escudo (en src/main/resources)
    private static final String ESCUDO_PATH   = "/ESCUDOCOLOR.jpg";

    // Cargar logo del ojo con el tamaño que yo quiera
    public static ImageIcon loadLogoOjoBuho(int width, int height) {
        return loadIcon(LOGO_OJO_PATH, width, height);
    }

    // Cargar escudo con el tamaño que yo quiera
    public static ImageIcon loadEscudo(int width, int height) {
        return loadIcon(ESCUDO_PATH, width, height);
    }

    // Método genérico para cargar y escalar una imagen
    private static ImageIcon loadIcon(String resourcePath, int width, int height) {
        try {
            // Busco el archivo dentro del classpath (resources)
            java.net.URL url = ImageUtils.class.getResource(resourcePath);
            if (url == null) {
                // Si no lo encuentra, lo aviso en la consola
                System.err.println("No se encontró la imagen en recursos: " + resourcePath);
                return null;
            }

            // Creo el icono base
            ImageIcon baseIcon = new ImageIcon(url);
            // Escalo la imagen al tamaño que necesito
            Image img = baseIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            // Regreso el icono ya escalado
            return new ImageIcon(img);
        } catch (Exception e) {
            // Cualquier error al cargar la imagen lo muestro aquí
            System.err.println("Error cargando imagen: " + resourcePath + " -> " + e.getMessage());
            return null;
        }
    }
}
