package org.dam2.adp.ecorastro.connection;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class Connection {
    private static Connection instance;
    private final SessionFactory sessionFactory;

    private Connection() {
        // Inicializamos la fábrica usando la unidad de persistencia que definimos en el XML

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("EcoRastroPU");
        // Extraemos la SessionFactory nativa de Hibernate
        this.sessionFactory = emf.unwrap(SessionFactory.class);
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
}