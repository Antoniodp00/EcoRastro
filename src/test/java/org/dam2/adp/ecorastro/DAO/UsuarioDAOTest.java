package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.model.Usuario;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioDAOTest {


    @Test
    void testGuardarUsuario() {
        // 1. Arrange
        UsuarioDAO dao = new UsuarioDAO();
        String emailUnico = "alumno_" + System.currentTimeMillis() + "@clase.com";

        Usuario usuario = new Usuario();
        usuario.setNombre("Alumno Ejemplo");
        usuario.setEmail(emailUnico);
        usuario.setContrasena("hibernate123");

        // 2. Act
        boolean resultado = dao.addUsuario(usuario);

        // 3. Assert
        assertTrue(resultado, "El método debería devolver true si guardó correctamente");
        assertNotNull(usuario.getId(), "El ID debería haberse generado");

        System.out.println("Usuario guardado ID: " + usuario.getId());
    }

    @Test
    void testBuscarPorEmail() {
        // 1. Arrange: Guardamos uno primero
        UsuarioDAO dao = new UsuarioDAO();
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