package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad {@link Usuario}.
 * <p>
 * Gestiona las operaciones CRUD relacionadas con los usuarios del sistema.
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class UsuarioDAO {

    private final String GET_ALL_HQL = "FROM Usuario";

    private  final String COUNT_USUARIOS_ACTIVOS = "SELECT COUNT(DISTINCT h.idUsuario) FROM Huella h";

    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * @param usuario El objeto Usuario a persistir.
     * @return true si la inserción fue exitosa, false en caso contrario.
     */
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

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param usuario El objeto Usuario con los datos modificados.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
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

    /**
     * Elimina un usuario de la base de datos.
     *
     * @param usuario El objeto Usuario a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
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

    /**
     * Recupera un usuario por su ID.
     *
     * @param id El ID del usuario.
     * @return El objeto Usuario encontrado o null.
     */
    public Usuario getUsuarioById(int id){
        try(Session session = Connection.getInstance().getSession()){
            return session.get(Usuario.class, id);
        }
    }

    /**
     * Busca un usuario por su correo electrónico.
     * <p>
     * Útil para el proceso de login y validación de duplicados.
     *
     * @param email El correo electrónico a buscar.
     * @return El objeto Usuario encontrado o null si no existe.
     */
    public Usuario getUsuarioByEmail(String email){
        try(Session session = Connection.getInstance().getSession()){
            return session.createQuery("FROM Usuario WHERE email = :email", Usuario.class)
                    .setParameter("email", email)
                    .uniqueResult();
        }
    }

    public Long countUsuariosActivos() {
        try (Session session = Connection.getInstance().getSession()) {
            // Cuenta usuarios que tengan al menos 1 huella
            return session.createQuery(COUNT_USUARIOS_ACTIVOS, Long.class).uniqueResult();
        }
    }
}