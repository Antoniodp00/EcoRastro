package org.dam2.adp.ecorastro.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    /**
     * Genera un hash seguro usando BCrypt.
     * El "Salt" se genera y guarda dentro del propio hash automáticamente.
     */
    public static String hashPassword(String passwordPlain) {
        return BCrypt.hashpw(passwordPlain, BCrypt.gensalt());
    }

    /**
     * Verifica si la contraseña plana coincide con el hash guardado.
     * @param passwordPlain La contraseña que escribe el usuario al loguearse.
     * @param storedHash El hash que trajimos de la base de datos.
     */
    public static boolean checkPassword(String passwordPlain, String storedHash) {
        if (storedHash == null || !storedHash.startsWith("$2a$")) {
            return false; // No es un hash BCrypt válido
        }
        return BCrypt.checkpw(passwordPlain, storedHash);
    }
}