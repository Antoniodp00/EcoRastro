package org.dam2.adp.ecorastro.service;

import org.dam2.adp.ecorastro.DAO.UsuarioDAO;
import org.dam2.adp.ecorastro.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioServiceTest {

    private Usuario usuarioPrueba;

    @AfterEach
    void tearDown() {
        // Limpiamos la BBDD después de cada prueba
        if (usuarioPrueba != null && usuarioPrueba.getId() != null) {
            new UsuarioDAO().deleteUsuario(usuarioPrueba);
        }
    }

    @Test
    void testRegistrarUsuarioNuevo() {
        // 1. Arrange
        UsuarioService service = new UsuarioService();
        usuarioPrueba = new Usuario("Nuevo User", "nuevo@test.com", "1234");

        // 2. Act
        boolean resultado = service.registrarUsuario(usuarioPrueba);

        // 3. Assert
        assertTrue(resultado, "Debe dejar registrar un email nuevo");
        assertNotNull(usuarioPrueba.getId());
    }

    @Test
    void testEvitarRegistroDuplicado() {
        // 1. Arrange: Registramos uno primero
        UsuarioService service = new UsuarioService();
        usuarioPrueba = new Usuario("Original", "duplicado@test.com", "1234");
        service.registrarUsuario(usuarioPrueba);

        // Intentamos registrar otro con EL MISMO EMAIL
        Usuario duplicado = new Usuario("Impostor", "duplicado@test.com", "9999");

        // 2. Act
        boolean resultado = service.registrarUsuario(duplicado);

        // 3. Assert
        assertFalse(resultado, "NO debe dejar registrar un email repetido");
    }

    @Test
    void testLoginCorrecto() {
        UsuarioService service = new UsuarioService();
        usuarioPrueba = new Usuario("Login User", "login@test.com", "secret");
        service.registrarUsuario(usuarioPrueba);

        // Intentamos login
        Usuario logueado = service.login("login@test.com", "secret");

        assertNotNull(logueado);
        assertEquals("Login User", logueado.getNombre());
    }

    @Test
    void testLoginIncorrecto() {
        UsuarioService service = new UsuarioService();
        // Probamos login con datos que no existen
        Usuario logueado = service.login("fantasma@test.com", "1234");
        assertNull(logueado, "Debe devolver null si no encuentra al usuario");
    }

    @Test
    void testActualizarUsuarioExitoso() {
        UsuarioService service = new UsuarioService();
        usuarioPrueba = new Usuario("User Update", "update@test.com", "1234");
        service.registrarUsuario(usuarioPrueba);

        usuarioPrueba.setNombre("Nombre Cambiado");
        boolean resultado = service.actualizarUsuario(usuarioPrueba, "nuevaPass");

        assertTrue(resultado);
        Usuario actualizado = service.login("update@test.com", "nuevaPass");
        assertNotNull(actualizado);
        assertEquals("Nombre Cambiado", actualizado.getNombre());
    }

    @Test
    void testActualizarUsuarioEmailDuplicado() {
        UsuarioService service = new UsuarioService();
        // Usuario 1
        usuarioPrueba = new Usuario("User 1", "user1@test.com", "1234");
        service.registrarUsuario(usuarioPrueba);

        // Usuario 2
        Usuario usuario2 = new Usuario("User 2", "user2@test.com", "1234");
        service.registrarUsuario(usuario2);

        // Intentamos cambiar el email de usuario2 al de usuario1
        usuario2.setEmail("user1@test.com");
        boolean resultado = service.actualizarUsuario(usuario2, null);

        assertFalse(resultado, "No debe permitir actualizar a un email ya existente");

        // Limpieza extra
        new UsuarioDAO().deleteUsuario(usuario2);
    }
}