package org.dam2.adp.ecorastro.util;

import org.dam2.adp.ecorastro.model.Usuario;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona la sesión del usuario y otros datos de sesión.
 * <p>
 * Implementa el patrón Singleton para mantener un estado global accesible desde toda la aplicación.
 * Almacena el usuario autenticado y permite guardar datos temporales arbitrarios.
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class SessionManager {

    private static SessionManager instance;
    private Usuario usuarioActual;
    private final Map<String, Object> sessionData = new HashMap<>();
    private static final Logger logger = Logger.getLogger(SessionManager.class.getName());

    private SessionManager() {}

    /**
     * Obtiene la instancia única de SessionManager.
     *
     * @return la instancia de SessionManager.
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
            logger.log(Level.INFO, "Instancia de SessionManager creada.");
        }
        return instance;
    }

    /**
     * Establece el usuario actual de la sesión.
     *
     * @param usuario el usuario actual.
     */
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        if (usuario != null) {
            logger.log(Level.INFO, "Usuario actual establecido: " + usuario.getNombre());
        } else {
            logger.log(Level.INFO, "Usuario actual establecido a null.");
        }
    }

    /**
     * Obtiene el usuario actual de la sesión.
     *
     * @return el usuario actual.
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Almacena un dato en la sesión.
     *
     * @param key la clave del dato.
     * @param value el valor del dato.
     */
    public void set(String key, Object value) {
        sessionData.put(key, value);
        logger.log(Level.FINE, "Dato de sesión almacenado: key='{" + key + "}', value='{" + value + "}'");
    }

    /**
     * Obtiene un dato de la sesión.
     *
     * @param key la clave del dato.
     * @return el valor del dato.
     */
    public Object get(String key) {
        Object value = sessionData.get(key);
        logger.log(Level.FINE, "Dato de sesión obtenido: key='{" + key + "}', value='{" + value + "}'");
        return value;
    }

    /**
     * Elimina un dato de la sesión.
     *
     * @param key la clave del dato a eliminar.
     */
    public void clear(String key) {
        sessionData.remove(key);
        logger.log(Level.FINE, "Dato de sesión eliminado: key='{" + key + "}'");
    }

    /**
     * Cierra la sesión actual, eliminando todos los datos.
     */
    public void cerrarSesion() {
        if (usuarioActual != null) {
            logger.log(Level.INFO, "Cerrando sesión para el usuario: " + usuarioActual.getNombre());
        } else {
            logger.log(Level.INFO, "Cerrando sesión (sin usuario activo).");
        }
        usuarioActual = null;
        sessionData.clear();
    }
}