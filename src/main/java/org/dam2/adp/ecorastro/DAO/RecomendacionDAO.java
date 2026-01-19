package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Recomendacion;
import org.hibernate.Session;

import java.util.List;

public class RecomendacionDAO {
    private final String GET_BY_CAT_HQL = "FROM Recomendacion r JOIN FETCH r.idCategoria WHERE r.idCategoria.nombre = :nombreCat";


    public List<Recomendacion> getRecomendacionesPorCategoria(String nombreCategoria) {
        try (Session session = Connection.getInstance().getSession()) {
            return session.createQuery(GET_BY_CAT_HQL, Recomendacion.class)
                    .setParameter("nombreCat", nombreCategoria)
                    .getResultList();
        }
    }
}
