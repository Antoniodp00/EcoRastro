package org.dam2.adp.ecorastro.service;

import org.dam2.adp.ecorastro.DAO.UsuarioDAO;
import org.dam2.adp.ecorastro.model.Usuario;
import org.dam2.adp.ecorastro.util.PasswordUtil;

public class UsuarioService {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

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


}
