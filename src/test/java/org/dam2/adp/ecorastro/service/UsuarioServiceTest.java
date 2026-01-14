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
}