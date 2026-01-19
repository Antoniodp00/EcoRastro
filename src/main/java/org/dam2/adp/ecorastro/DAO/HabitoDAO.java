package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Habito;
import org.dam2.adp.ecorastro.model.HabitoId;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class HabitoDAO {
    private final String GET_BY_USER_HQL = "FROM Habito h where h.idUsuario.id = :idUsuario";


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

        }finally {
            if (session != null){
                session.close();
            }
        }
        return insertado;

    }

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

    public Habito getHabitoById(HabitoId id) {
        try (Session session = Connection.getInstance().getSession()) {
            return session.get(Habito.class, id);
        }
    }

    public List<Habito> getHabitosByUsuario(int idUsuario) {
        try (Session session = Connection.getInstance().getSession()) {
            return session.createQuery(GET_BY_USER_HQL, Habito.class)
                    .setParameter("idUsuario", idUsuario)
                    .getResultList();
        }

    }
}
