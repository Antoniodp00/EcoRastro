package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Recomendacion;
import org.hibernate.Session;

import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad {@link Recomendacion}.
 * <p>
 * Permite consultar recomendaciones filtradas por categoría.
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class RecomendacionDAO {
    /** Consulta HQL para obtener recomendaciones de una categoría específica. */
    private final String GET_BY_CAT_HQL = "FROM Recomendacion r JOIN FETCH r.idCategoria WHERE r.idCategoria.nombre = :nombreCat";

    /**
     * Obtiene una lista de recomendaciones asociadas a una categoría dada.
     *
     * @param nombreCategoria El nombre de la categoría (ej: "Agua").
     * @return Lista de recomendaciones encontradas.
     */
    public List<Recomendacion> getRecomendacionesPorCategoria(String nombreCategoria) {
        try (Session session = Connection.getInstance().getSession()) {
            return session.createQuery(GET_BY_CAT_HQL, Recomendacion.class)
                    .setParameter("nombreCat", nombreCategoria)
                    .getResultList();
        }
    }
}