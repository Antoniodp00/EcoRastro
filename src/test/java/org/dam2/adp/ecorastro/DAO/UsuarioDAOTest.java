package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioDAOTest {

    // Esta es la variable "global" que usa el tearDown
    private Usuario usuarioCreado;

    @AfterEach
    void tearDown() {
        // Verifica si la variable global tiene algo y si tiene ID (significa que se guardó)
        if (usuarioCreado != null && usuarioCreado.getId() != null) {
            UsuarioDAO dao = new UsuarioDAO();

            // Verificamos si sigue existiendo antes de borrar (por si el test ya lo borró)
            if (dao.getUsuarioById(usuarioCreado.getId()) != null) {
                dao.deleteUsuario(usuarioCreado);
                System.out.println("Limpieza: Usuario de prueba eliminado.");
            }
        }
    }

    @Test
    void testGuardarUsuario() {
        // 1. Arrange
        UsuarioDAO dao = new UsuarioDAO();
        String emailUnico = "alumno_" + System.currentTimeMillis() + "@clase.com";


        usuarioCreado = new Usuario(); // <--- Asignamos a la variable global
        usuarioCreado.setNombre("Alumno Ejemplo");
        usuarioCreado.setEmail(emailUnico);
        usuarioCreado.setContrasena("hibernate123");

        // 2. Act
        boolean resultado = dao.addUsuario(usuarioCreado);

        // 3. Assert
        assertTrue(resultado);
        assertNotNull(usuarioCreado.getId());
    }

    @Test
    void testBuscarPorEmail() {
        UsuarioDAO dao = new UsuarioDAO();
        String email = "busqueda_" + System.currentTimeMillis() + "@test.com";

        Usuario user = new Usuario("Busqueda", email, "123");
        usuarioCreado = user;

        dao.addUsuario(user);

        Usuario encontrado = dao.getUsuarioByEmail(email);

        assertNotNull(encontrado);
        assertEquals(email, encontrado.getEmail());
    }

    @Test
    void testBuscarPorId() {
        UsuarioDAO dao = new UsuarioDAO();
        String email = "busquedaID_" + System.currentTimeMillis() + "@test.com";

        Usuario user = new Usuario("Busqueda_ID", email,"123");
        usuarioCreado = user;

        dao.addUsuario(user);

        Usuario encontrado = dao.getUsuarioById(user.getId());

        assertNotNull(encontrado);
        assertEquals(user.getId(), encontrado.getId());
    }

    @Test
    void testActualizarUsuario() {
        UsuarioDAO dao = new UsuarioDAO();
        String email = "update_" + System.currentTimeMillis() + "@test.com";

        Usuario user = new Usuario("Actualizacion", email, "123");
        usuarioCreado = user;

        dao.addUsuario(user);


        user.setNombre("Actualizado");
        boolean resultado = dao.updateUsuario(user);

        assertTrue(resultado);
        assertEquals("Actualizado", dao.getUsuarioById(user.getId()).getNombre());
    }

    @Test
    void testEliminarUsuario() {

        UsuarioDAO dao = new UsuarioDAO();
        String email = "delete_" + System.currentTimeMillis() + "@test.com";

        Usuario user = new Usuario("Eliminacion", email, "123");
        // Lo asignamos a usuarioCreado por si el test FALLA antes de borrarlo.
        // Si falla antes, el tearDown lo limpiará.
        usuarioCreado = user;

        dao.addUsuario(user);

        // Ejecutamos el borrado manual
        boolean resultado = dao.deleteUsuario(user);

        assertTrue(resultado);
        assertNull(dao.getUsuarioById(user.getId()));

        // Como ya lo hemos borrado con éxito, ponemos la variable a null
        // para que el tearDown no intente borrarlo otra vez
        usuarioCreado = null;
    }
}