package org.dam2.adp.ecorastro.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para el manejo seguro de contraseñas.
 * <p>
 * Utiliza el algoritmo BCrypt para generar hashes y verificar contraseñas.
 *
 * @author TuNombre
 * @version 1.0
 */
public class PasswordUtil {

    /**
     * Genera un hash seguro usando BCrypt.
     * <p>
     * El "Salt" se genera y guarda dentro del propio hash automáticamente.
     *
     * @param passwordPlain La contraseña en texto plano.
     * @return El hash resultante listo para almacenar en BBDD.
     */
    public static String hashPassword(String passwordPlain) {
        return BCrypt.hashpw(passwordPlain, BCrypt.gensalt());
    }

    /**
     * Verifica si la contraseña plana coincide con el hash guardado.
     *
     * @param passwordPlain La contraseña que escribe el usuario al loguearse.
     * @param storedHash El hash que trajimos de la base de datos.
     * @return true si coinciden, false si no.
     */
    public static boolean checkPassword(String passwordPlain, String storedHash) {
        if (storedHash == null || !storedHash.startsWith("$2a$")) {
            return false; // No es un hash BCrypt válido
        }
        return BCrypt.checkpw(passwordPlain, storedHash);
    }
}