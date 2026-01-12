package org.dam2.adp.ecorastro.dao;

import org.dam2.adp.ecorastro.model.Usuario;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioDaoTest {


    @Test
    void testGuardarUsuario() {
        // 1. Arrange
        UsuarioDao dao = new UsuarioDao();
        String emailUnico = "alumno_" + System.currentTimeMillis() + "@clase.com";

        Usuario usuario = new Usuario();
        usuario.setNombre("Alumno Ejemplo");
        usuario.setEmail(emailUnico);
        usuario.setContrasena("hibernate123");

        // 2. Act
        boolean resultado = dao.addUsuario(usuario);

        // 3. Assert
        assertTrue(resultado, "El método debería devolver true si guardó correctamente");
        assertNotNull(usuario.getIdUsuario(), "El ID debería haberse generado");

        System.out.println("Usuario guardado ID: " + usuario.getIdUsuario());
    }

    @Test
    void testBuscarPorEmail() {
        // 1. Arrange: Guardamos uno primero
        UsuarioDao dao = new UsuarioDao();
        String email = "busqueda_" + System.currentTimeMillis() + "@test.com";
        Usuario user = new Usuario("Busqueda", email, "123");
        dao.addUsuario(user);

        // 2. Act: Lo buscamos
        Usuario encontrado = dao.getUsuarioByEmail(email);

        // 3. Assert
        assertNotNull(encontrado);
        assertEquals(email, encontrado.getEmail());
    }
}