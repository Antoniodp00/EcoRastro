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

public class HuellaDAO {

    private final String GET_BY_USERID_HQL = "FROM Huella h JOIN FETCH h.idActividad a JOIN FETCH a.idCategoria WHERE h.idUsuario.id = :id_usuario";
    private final String GET_HUELLA_POR_FECHA = "FROM Huella h " +
            "JOIN FETCH h.idActividad a " +
            "JOIN FETCH a.idCategoria " +
            "WHERE h.idUsuario.id = :uid " +
            "AND h.fecha >= :inicio AND h.fecha <= :fin " +
            "ORDER BY h.fecha DESC";

    private final String GET_MEDIA_IMPACTO_POR_CATEGORIA = "SELECT c.nombre, AVG(h.valor * c.factorEmision) " +
            "FROM Huella h " +
            "JOIN h.idActividad a " +
            "JOIN a.idCategoria c " +
            "GROUP BY c.nombre";

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

    public List<Huella> getHuellasPorUsuario(int idUsuario) {
        List<Huella> huellas = null;
        try (Session session = Connection.getInstance().getSession()) {
            Query<Huella> query = session.createQuery(GET_BY_USERID_HQL, Huella.class);
            query.setParameter("id_usuario", idUsuario);
            huellas = query.getResultList();

            return huellas;

        }
    }

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
}

