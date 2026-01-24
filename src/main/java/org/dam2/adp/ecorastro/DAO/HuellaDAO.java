package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Huella;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase de Acceso a Datos (DAO) para la entidad {@link Huella}.
 * <p>
 * Gestiona las operaciones CRUD y consultas específicas contra la base de datos usando Hibernate.
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class HuellaDAO {

    /** Consulta HQL para obtener huellas de un usuario con relaciones cargadas. */
    private final String GET_BY_USERID_HQL = "FROM Huella h JOIN FETCH h.idActividad a JOIN FETCH a.idCategoria WHERE h.idUsuario.id = :id_usuario";

    /** Consulta HQL para filtrar huellas por rango de fechas. */
    private final String GET_HUELLA_POR_FECHA = "FROM Huella h " +
            "JOIN FETCH h.idActividad a " +
            "JOIN FETCH a.idCategoria " +
            "WHERE h.idUsuario.id = :uid " +
            "AND h.fecha >= :inicio AND h.fecha <= :fin " +
            "ORDER BY h.fecha DESC";

    /** Consulta HQL para calcular la media de impacto por categoría. */
    private final String GET_MEDIA_IMPACTO_POR_CATEGORIA = "SELECT c.nombre, AVG(h.valor * c.factorEmision) " +
            "FROM Huella h " +
            "JOIN h.idActividad a " +
            "JOIN a.idCategoria c " +
            "GROUP BY c.nombre";

    /** Consulta HQL para calcular la media de impacto por categoría filtrada por fecha. */
    private final String GET_MEDIA_IMPACTO_POR_CATEGORIA_FECHA = "SELECT c.nombre, AVG(h.valor * c.factorEmision) " +
            "FROM Huella h " +
            "JOIN h.idActividad a " +
            "JOIN a.idCategoria c " +
            "WHERE h.fecha >= :inicio AND h.fecha <= :fin " +
            "GROUP BY c.nombre";

    /**
     * Inserta una nueva huella en la base de datos.
     *
     * @param huella El objeto Huella a persistir.
     * @return true si la inserción fue exitosa, false en caso contrario.
     */
    public boolean addHuella(Huella huella) {
        boolean insertada = false;
        Transaction tx = null;

        try (Session session = Connection.getInstance().getSession()) {
            tx = session.beginTransaction();
            session.persist(huella);
            tx.commit();
            insertada = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();

        }
        return insertada;
    }

    /**
     * Actualiza los datos de una huella existente.
     *
     * @param huella El objeto Huella con los datos actualizados.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean updateHuella(Huella huella) {
        boolean actualizada = false;
        Transaction tx = null;

        try (Session session = Connection.getInstance().getSession()) {
            tx = session.beginTransaction();
            session.merge(huella);
            tx.commit();
            actualizada = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return actualizada;

    }

    /**
     * Elimina una huella de la base de datos.
     *
     * @param huella El objeto Huella a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean deleteHuella(Huella huella) {
        boolean eliminada = false;
        Transaction tx = null;

        try(Session session = Connection.getInstance().getSession()){
            tx = session.beginTransaction();
            session.remove(huella);
            tx.commit();
            eliminada = true;
        }catch (Exception e){
            if (tx != null && tx.isActive()){
                tx.rollback();
            }
            e.printStackTrace();
        }
        return eliminada;
    }

    /**
     * Recupera todas las huellas asociadas a un usuario específico.
     * <p>
     * Utiliza JOIN FETCH para cargar las relaciones de Actividad y Categoría de forma eficiente.
     *
     * @param idUsuario El ID del usuario.
     * @return Lista de huellas del usuario.
     */
    public List<Huella> getHuellasPorUsuario(int idUsuario) {
        List<Huella> huellas = null;
        try (Session session = Connection.getInstance().getSession()) {
            Query<Huella> query = session.createQuery(GET_BY_USERID_HQL, Huella.class);
            query.setParameter("id_usuario", idUsuario);
            huellas = query.getResultList();

            return huellas;

        }
    }

    /**
     * Recupera las huellas de un usuario filtradas por un rango de fechas.
     *
     * @param idUsuario   El ID del usuario.
     * @param fechaInicio Fecha de inicio del filtro.
     * @param fechaFin    Fecha de fin del filtro.
     * @return Lista de huellas que cumplen los criterios.
     */
    public List<Huella> getHuellasPorFecha(int idUsuario, LocalDate fechaInicio, LocalDate fechaFin) {
        try(Session session = Connection.getInstance().getSession()){
            Instant inicio = fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant fin = fechaFin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant();

            return session.createQuery(GET_HUELLA_POR_FECHA, Huella.class)
                    .setParameter("uid", idUsuario)
                    .setParameter("inicio", inicio)
                    .setParameter("fin", fin)
                    .getResultList();
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Calcula la media del impacto de carbono por categoría para todos los registros.
     *
     * @return Un mapa con el nombre de la categoría y su impacto medio.
     */
    public Map<String, Double> getMediaImpactoPorCategoria() {
        Map<String, Double> medias = new HashMap<>();
        try (Session session = Connection.getInstance().getSession()) {
            List<Object[]> resultados = session.createQuery(GET_MEDIA_IMPACTO_POR_CATEGORIA, Object[].class).getResultList();
            for (Object[] fila : resultados) {
                medias.put((String) fila[0], (Double) fila[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return medias;
    }

    /**
     * Calcula la media del impacto de carbono por categoría dentro de un rango de fechas.
     *
     * @param fechaInicio Fecha de inicio del rango.
     * @param fechaFin    Fecha de fin del rango.
     * @return Un mapa con el nombre de la categoría y su impacto medio en ese periodo.
     */
    public Map<String, Double> getMediaImpactoPorCategoriaFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Double> medias = new HashMap<>();
        try (Session session = Connection.getInstance().getSession()) {
            Instant inicio = fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant fin = fechaFin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant();

            List<Object[]> resultados = session.createQuery(GET_MEDIA_IMPACTO_POR_CATEGORIA_FECHA, Object[].class)
                    .setParameter("inicio", inicio)
                    .setParameter("fin", fin)
                    .getResultList();

            for (Object[] fila : resultados) {
                medias.put((String) fila[0], (Double) fila[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return medias;
    }
}