package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Actividad;
import org.hibernate.Session;

import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad {@link Actividad}.
 * <p>
 * Proporciona métodos para recuperar el catálogo de actividades disponibles.
 *
 * @author TuNombre
 * @version 1.0
 */
public class ActividadDAO {
    /** Consulta HQL para obtener todas las actividades cargando su categoría. */
    private final String GET_ALL_HQL = "FROM Actividad a JOIN FETCH a.idCategoria";//JOIN FETCHcarga inmediatamente las entidades relacionadas, inicializándolas en el objeto principal.

    /**
     * Recupera todas las actividades registradas en la base de datos.
     * <p>
     * Incluye la información de la categoría asociada mediante {@code JOIN FETCH}.
     *
     * @return Lista de todas las actividades.
     */
    public List<Actividad> getAllActividades() {
        try (Session session = Connection.getInstance().getSession()) {
            return session.createQuery(GET_ALL_HQL, Actividad.class).getResultList();

        }
    }
}