package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class UsuarioDAO {

    private final String GET_ALL_HQL = "FROM Usuario";

    public boolean addUsuario(Usuario usuario) {

        boolean insertado = false;
        Transaction tx = null;

        try (Session session = Connection.getInstance().getSession()){
            tx = session.beginTransaction();
            session.persist(usuario);
            tx.commit();
            insertado = true;

        }catch (Exception e){
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        }

        return insertado;
    }


    public boolean updateUsuario(Usuario usuario) {
        boolean actualizado = false;
        Transaction tx = null;

        try (Session session = Connection.getInstance().getSession()) {
            tx = session.beginTransaction();
            session.merge(usuario);
            tx.commit();
            actualizado = true;
        }catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return actualizado;
    }


    public boolean deleteUsuario(Usuario usuario) {
        boolean eliminado = false;
        Transaction tx = null;

        try (Session session = Connection.getInstance().getSession()) {
            tx = session.beginTransaction();
            session.remove(usuario);
            tx.commit();
            eliminado = true;
        }catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return eliminado;
    }

    public Usuario getUsuarioById(int id){
        try(Session session = Connection.getInstance().getSession()){
            return session.get(Usuario.class, id);
        }
    }

    public Usuario getUsuarioByEmail(String email){
        try(Session session = Connection.getInstance().getSession()){
            return session.createQuery("FROM Usuario WHERE email = :email", Usuario.class)
                    .setParameter("email", email)
                    .uniqueResult();
        }
    }

}
