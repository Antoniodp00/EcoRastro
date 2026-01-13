package org.dam2.adp.ecorastro.connection;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Connection {
    private static Connection instance;
    private SessionFactory sessionFactory;

    private Connection() {
        try {
            // Esto lee automáticamente el archivo "hibernate.cfg.xml" de resources
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al iniciar Hibernate: " + e.getMessage());
        }
    }

    public static Connection getInstance() {
        if (instance == null) {
            instance = new Connection();
        }
        return instance;
    }

    public Session getSession() {
        return sessionFactory.openSession();
    }

    // Método útil para cerrar la fábrica al salir de la app
    public void close() {
        if (sessionFactory != null && sessionFactory.isOpen()) {
            sessionFactory.close();
        }
    }
}