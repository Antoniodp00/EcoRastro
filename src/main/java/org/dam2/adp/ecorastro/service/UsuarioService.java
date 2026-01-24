package org.dam2.adp.ecorastro.service;

import org.dam2.adp.ecorastro.DAO.UsuarioDAO;
import org.dam2.adp.ecorastro.model.Usuario;
import org.dam2.adp.ecorastro.util.PasswordUtil;

/**
 * Servicio que gestiona la lógica de negocio relacionada con los usuarios.
 * <p>
 * Se encarga del registro, autenticación (login) y seguridad de las contraseñas.
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class UsuarioService {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Registra un nuevo usuario en el sistema.
     * <p>
     * Verifica que el email no esté duplicado y cifra la contraseña antes de guardar.
     *
     * @param usuario El objeto usuario con los datos del formulario.
     * @return true si el registro fue exitoso, false si el email ya existe.
     */
    public boolean registrarUsuario(Usuario usuario) {
        Usuario existente = usuarioDAO.getUsuarioByEmail(usuario.getEmail());
        if (existente != null) {
            return false; // El email ya está registrado

        }

        //1. Encriptamos la password antes de guardar al usuario
        String passEncriptada = PasswordUtil.hashPassword(usuario.getContrasena());
        usuario.setContrasena(passEncriptada);

        return usuarioDAO.addUsuario(usuario);

    }

    /**
     * Autentica a un usuario en el sistema.
     * <p>
     * Comprueba si el email existe y si la contraseña proporcionada coincide con la almacenada (hash).
     *
     * @param email         El correo electrónico del usuario.
     * @param passwordPlain La contraseña en texto plano introducida por el usuario.
     * @return El objeto Usuario si las credenciales son válidas, o null si fallan.
     */
    public Usuario login(String email, String passwordPlain) {
        Usuario usuario = usuarioDAO.getUsuarioByEmail(email);

        if(usuario == null){
            return null;
        }

        if(PasswordUtil.checkPassword(passwordPlain, usuario.getContrasena())){
            return usuario;
        }

        return null;
    }

    /**
     * Actualiza los datos de un usuario existente.
     * <p>
     * Gestiona la actualización del perfil, incluyendo validación de email único
     * y cifrado de nueva contraseña si se proporciona.
     *
     * @param usuario       El objeto usuario con los datos actualizados.
     * @param nuevaPassword La nueva contraseña en texto plano (opcional).
     *                      Si es null o vacía, se mantiene la contraseña actual.
     * @return true si la actualización fue exitosa, false si el email ya está en uso.
     */
    public boolean actualizarUsuario(Usuario usuario, String nuevaPassword) {
        // 1. Si cambia el email, verificar que no esté ocupado por otro
        Usuario existente = usuarioDAO.getUsuarioByEmail(usuario.getEmail());
        if (existente != null && !existente.getId().equals(usuario.getId())) {
            return false; // El email ya pertenece a otra persona
        }

        // 2. Gestión de la contraseña
        if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
            String hash = PasswordUtil.hashPassword(nuevaPassword);
            usuario.setContrasena(hash);
        }

        // 3. Guardar cambios en BBDD
        return usuarioDAO.updateUsuario(usuario);
    }
}