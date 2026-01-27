package org.dam2.adp.ecorastro.connection;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Clase Singleton encargada de gestionar la conexión con la base de datos a través de Hibernate.
 * <p>
 * Proporciona acceso centralizado a la {@link SessionFactory} y permite obtener sesiones
 * individuales para realizar operaciones de base de datos.
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class Connection {
    private static Connection instance;
    private SessionFactory sessionFactory;

    /**
     * Constructor privado para implementar el patrón Singleton.
     * <p>
     * Inicializa la SessionFactory leyendo la configuración del archivo {@code hibernate.cfg.xml}.
     */
    private Connection() {
        try {
            // Esto lee automáticamente el archivo "hibernate.cfg.xml" de resources
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Error CRÍTICO al iniciar Hibernate: " + e.getMessage());
            // Si falla la conexión, es mejor saberlo aquí.
            throw new RuntimeException("No se pudo iniciar Hibernate", e);
        }
    }

    /**
     * Obtiene la instancia única de la clase Connection.
     *
     * @return La instancia Singleton de Connection.
     */
    public static Connection getInstance() {
        if (instance == null) {
            instance = new Connection();
        }
        return instance;
    }

    /**
     * Abre y devuelve una nueva sesión de Hibernate.
     *
     * @return Una nueva sesión (Session).
     * @throws IllegalStateException Si la SessionFactory no se ha inicializado correctamente.
     */
    public Session getSession() {
        if (sessionFactory == null) {
             throw new IllegalStateException("SessionFactory es NULL. La inicialización falló previamente.");
        }
        return sessionFactory.openSession();
    }

    /**
     * Cierra la SessionFactory.
     * <p>
     * Debe llamarse al cerrar la aplicación para liberar recursos de conexión.
     */
    public void close() {
        if (sessionFactory != null && sessionFactory.isOpen()) {
            sessionFactory.close();
        }
    }
}