package mx.unison.monitor;

import java.awt.*;

// Maneja el tema actual y regresa colores según sea claro u oscuro
public class ThemeManager {

    // Tema actual (empiezo con claro, puedes cambiar si quieres)
    private static Theme currentTheme = Theme.LIGHT;

    // Colores base para tema claro
    private static final Color BG_LIGHT = Color.WHITE;                      // Fondo claro
    private static final Color CARD_LIGHT = new Color(245, 245, 245);      // Paneles claros
    private static final Color TEXT_LIGHT = new Color(40, 40, 40);         // Texto oscuro

    // Colores base para tema oscuro
    private static final Color BG_DARK = new Color(18, 18, 18);            // Fondo oscuro
    private static final Color CARD_DARK = new Color(34, 34, 34);          // Paneles oscuros
    private static final Color TEXT_DARK = new Color(230, 230, 230);       // Texto claro

    // Gris que tú pediste (#BBBBBB aprox)
    private static final Color ACCENT_GRAY = new Color(0xBB, 0xBB, 0xBB);

    // Azul UNISON (puedes ajustar si quieres otro tono)
    private static final Color PRIMARY_COLOR = new Color(0, 63, 135);

    // Dorado UNISON
    private static final Color SECONDARY_COLOR = new Color(212, 175, 55);

    // Regresar tema actual
    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    // Cambiar de claro a oscuro o viceversa
    public static void toggleTheme() {
        // Si está en claro, pasa a oscuro, si no, pasa a claro
        currentTheme = (currentTheme == Theme.LIGHT) ? Theme.DARK : Theme.LIGHT;
    }

    // Color de fondo principal (ventana)
    public static Color getBackgroundColor() {
        return (currentTheme == Theme.LIGHT) ? BG_LIGHT : BG_DARK;
    }

    // Color de fondo para paneles / tarjetas
    public static Color getCardBackgroundColor() {
        return (currentTheme == Theme.LIGHT) ? CARD_LIGHT : CARD_DARK;
    }

    // Color de texto según tema
    public static Color getTextColor() {
        return (currentTheme == Theme.LIGHT) ? TEXT_LIGHT : TEXT_DARK;
    }

    // Gris de acento (#BBBBBB)
    public static Color getAccentGray() {
        return ACCENT_GRAY;
    }

    // Azul UNISON
    public static Color getPrimaryColor() {
        return PRIMARY_COLOR;
    }

    // Dorado UNISON
    public static Color getSecondaryColor() {
        return SECONDARY_COLOR;
    }
}
