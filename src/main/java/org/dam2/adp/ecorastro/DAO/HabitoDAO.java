package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Habito;
import org.dam2.adp.ecorastro.model.HabitoId;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad {@link Habito}.
 * <p>
 * Gestiona las operaciones CRUD y consultas específicas contra la base de datos usando Hibernate.
 *
 * @author TuNombre
 * @version 1.0
 */
public class HabitoDAO {

    /** Consulta HQL para obtener hábitos de un usuario con sus relaciones cargadas. */
    private final String GET_BY_USER_HQL = "FROM Habito h " +
            "JOIN FETCH h.idActividad a " +
            "JOIN FETCH a.idCategoria " +
            "WHERE h.idUsuario.id = :idUsuario";

    /** Consulta HQL para obtener el hábito más frecuente de un usuario. */
    private final String GET_HABITO_MAS_FRECUENTE_HQL = "FROM Habito h " +
            "JOIN FETCH h.idActividad a " +
            "JOIN FETCH a.idCategoria " +
            "WHERE h.idUsuario.id = :uid " +
            "ORDER BY h.frecuencia DESC";

    /**
     * Inserta o actualiza un hábito en la base de datos.
     *
     * @param habito El objeto Habito a persistir.
     * @return true si la operación fue exitosa, false en caso contrario.
     */
    public boolean addHabito(Habito habito) {
        Transaction tx = null;
        boolean insertado = false;
        Session session = null;
        try {
            session = Connection.getInstance().getSession();
            tx = session.beginTransaction();
            session.merge(habito);
            tx.commit();
            insertado = true;

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();

        } finally {
            if (session != null) {
                session.close();
            }
        }
        return insertado;

    }

    /**
     * Elimina un hábito de la base de datos.
     *
     * @param habito El objeto Habito a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean deleteHabito(Habito habito) {
        boolean eliminado = false;
        Transaction tx = null;
        try (Session session = Connection.getInstance().getSession()) {
            tx = session.beginTransaction();
            session.remove(session.contains(habito) ? habito : session.merge(habito));
            tx.commit();
            eliminado = true;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return eliminado;

    }

    /**
     * Recupera un hábito por su ID compuesto.
     *
     * @param id El ID compuesto (Usuario + Actividad).
     * @return El hábito encontrado o null.
     */
    public Habito getHabitoById(HabitoId id) {
        try (Session session = Connection.getInstance().getSession()) {
            return session.get(Habito.class, id);
        }
    }

    /**
     * Obtiene todos los hábitos de un usuario.
     *
     * @param idUsuario El ID del usuario.
     * @return Lista de hábitos.
     */
    public List<Habito> getHabitosByUsuario(int idUsuario) {
        try (Session session = Connection.getInstance().getSession()) {
            return session.createQuery(GET_BY_USER_HQL, Habito.class)
                    .setParameter("idUsuario", idUsuario)
                    .getResultList();
        }

    }

    /**
     * Obtiene el hábito con mayor frecuencia registrado por un usuario.
     *
     * @param idUsuario El ID del usuario.
     * @return El hábito más frecuente o null si no existen registros.
     */
    public Habito getHabitoMasFrecuente(int idUsuario) {

        try (Session session = Connection.getInstance().getSession()) {
            List<Habito> habitos = session.createQuery(GET_HABITO_MAS_FRECUENTE_HQL, Habito.class)
                    .setParameter("uid", idUsuario)
                    .setMaxResults(1) // Solo queremos el primero (el más frecuente)
                    .getResultList();

            return habitos.isEmpty() ? null : habitos.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}