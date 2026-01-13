package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UsuarioDAO {

    private final String GET_USUARIO_BY_EMAIL = "FROM Usuario WHERE email = :email";

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

    public Usuario getUsuarioById(int id){
        try(Session session = Connection.getInstance().getSession()){
            return session.get(Usuario.class, id);
        }
    }

    public Usuario getUsuarioByEmail(String email){
        try(Session session = Connection.getInstance().getSession()){
            return (Usuario) session.createQuery(GET_USUARIO_BY_EMAIL, Usuario.class)
                    .setParameter("email", email);
        }
    }
}
