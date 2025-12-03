package mx.unison.monitor;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utilidad para encriptar y desencriptar cadenas usando AES.
 *
 * El protocolo del servidor/cliente envía líneas encriptadas (Base64) por sockets.
 * Esto satisface el requisito de "encriptar" antes de enviar.
 */
public class EncryptionUtils {

    // Clave y vector de inicialización fijos para simplificar la práctica
    private static final String SECRET_KEY = "UnisonMonitorKey"; // 16 caracteres
    private static final String INIT_VECTOR = "1234567890ABCDEF"; // 16 caracteres

    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            ServerLogWindow.log("Error encriptando: " + ex.getMessage());
            return "";
        }
    }

    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ServerLogWindow.log("Error desencriptando: " + ex.getMessage());
            return "";
        }
    }
}
