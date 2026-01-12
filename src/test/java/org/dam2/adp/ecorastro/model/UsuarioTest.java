package org.dam2.adp.ecorastro.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void testCrearUsuario() {
        // 1. PREPARACIÓN (Arrange)
        // Definimos los datos que queremos probar
        String nombre = "Juan Perez";
        String email = "juan@email.com";
        String pass = "123456";

        // 2. EJECUCIÓN (Act)
        // Intentamos crear el objeto Usuario (esto fallará si no se ha creado la clase Usuario aún)
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setContrasena(pass);

        // 3. VERIFICACIÓN (Assert)
        // Comprobamos que los datos se guardaron bien en el objeto
        assertEquals("Juan Perez", usuario.getNombre());
        assertEquals("juan@email.com", usuario.getEmail());
        assertEquals("123456", usuario.getContrasena());
    }
}