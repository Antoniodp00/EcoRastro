package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Huella;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class HuellaDAO {

    private final String GET_BY_USERID_HQL = "FROM Huella h JOIN FETCH h.idActividad a JOIN FETCH a.idCategoria WHERE h.idUsuario.id = :id_usuario";

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
}

