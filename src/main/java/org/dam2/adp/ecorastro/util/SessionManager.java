package org.dam2.adp.ecorastro.util;

import org.dam2.adp.ecorastro.model.Usuario;

import java.util.HashMap;
import java.util.Map;

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

    private SessionManager() {}

    /**
     * Obtiene la instancia única de SessionManager.
     *
     * @return la instancia de SessionManager.
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
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
    }

    /**
     * Obtiene un dato de la sesión.
     *
     * @param key la clave del dato.
     * @return el valor del dato.
     */
    public Object get(String key) {
        return sessionData.get(key);
    }

    /**
     * Elimina un dato de la sesión.
     *
     * @param key la clave del dato a eliminar.
     */
    public void clear(String key) {
        sessionData.remove(key);
    }

    /**
     * Cierra la sesión actual, eliminando todos los datos.
     */
    public void cerrarSesion() {
        usuarioActual = null;
        sessionData.clear();
    }
}