package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Actividad;
import org.hibernate.Session;

import java.util.List;

public class ActividadDAO {
    private final String GET_ALL_HQL = "FROM Actividad a JOIN FETCH a.idCategoria";//JOIN FETCHcarga inmediatamente las entidades relacionadas, inicializándolas en el objeto principal.


    public List<Actividad> getAllActividades() {
        try (Session session = Connection.getInstance().getSession()) {
            return session.createQuery(GET_ALL_HQL, Actividad.class).getResultList();

        }
    }
}
