package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Actividad;
import org.dam2.adp.ecorastro.model.Categoria;
import org.dam2.adp.ecorastro.model.Huella;
import org.dam2.adp.ecorastro.model.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HuellaDAOTest {

    private HuellaDAO huellaDAO;

    // Variables para los datos de prueba
    private Usuario usuarioTest;
    private Categoria categoriaTest;
    private Actividad actividadTest;
    private Huella huellaGuardada; // Referencia para borrarla al final

    @BeforeEach
    void setUp() {
        huellaDAO = new HuellaDAO();
        crearDatosDePrueba();
    }

    @AfterEach
    void tearDown() {
        limpiarDatosDePrueba();
    }

    @Test
    void testGuardarHuella() {
        // 1. Arrange: Preparamos una huella nueva
        Huella huella = new Huella();
        huella.setIdUsuario(usuarioTest);
        huella.setIdActividad(actividadTest);
        huella.setValor(new BigDecimal("10.5")); // 10.5 km
        huella.setUnidad("km");
        huella.setFecha(Instant.now());

        // 2. Act: Guardamos
        boolean resultado = huellaDAO.addHuella(huella);

        // Guardamos la referencia para que el tearDown la borre luego
        if (resultado) {
            huellaGuardada = huella;
        }

        // 3. Assert: Verificamos
        assertTrue(resultado, "El método guardarHuella debería devolver true");
        assertNotNull(huella.getId(), "La huella guardada debe tener un ID generado por la BBDD");
    }

    @Test
    void testObtenerHuellasPorUsuario() {
        // 1. Arrange: Guardamos una huella manualmente primero
        Huella h = new Huella();
        h.setIdUsuario(usuarioTest);
        h.setIdActividad(actividadTest);
        h.setValor(BigDecimal.TEN);
        h.setUnidad("kg");
        h.setFecha(Instant.now());
        huellaDAO.addHuella(h);
        huellaGuardada = h;

        // 2. Act: Buscamos las huellas de este usuario
        List<Huella> huellas = huellaDAO.getHuellasPorUsuario(usuarioTest.getId());

        // 3. Assert
        assertFalse(huellas.isEmpty(), "La lista no debería estar vacía");
        assertEquals(1, huellas.size(), "Debería haber 1 huella");
        assertEquals(usuarioTest.getId(), huellas.get(0).getIdUsuario().getId(), "El usuario de la huella debe coincidir");
    }

    // --- MÉTODOS AUXILIARES (SETUP / TEARDOWN) ---

    private void crearDatosDePrueba() {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();

            // Necesitamos crear toda la cadena de dependencias:
            // Categoria -> Actividad -> Usuario -> Huella

            usuarioTest = new Usuario("Test Huella", "huella@test.com", "1234");
            session.persist(usuarioTest);

            categoriaTest = new Categoria();
            categoriaTest.setNombre("Categoria Test");
            categoriaTest.setFactorEmision(new BigDecimal("0.5")); // 0.5 kg CO2 por unidad
            categoriaTest.setUnidad("ud");
            session.persist(categoriaTest);

            actividadTest = new Actividad();
            actividadTest.setNombre("Actividad Test");
            actividadTest.setIdCategoria(categoriaTest);
            session.persist(actividadTest);

            tx.commit();
        }
    }

    private void limpiarDatosDePrueba() {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();

            // Borramos en orden inverso para no romper FK (Foreign Keys)
            if (huellaGuardada != null && huellaGuardada.getId() != null) {
                session.remove(session.merge(huellaGuardada));
            }
            if (actividadTest != null) session.remove(session.merge(actividadTest));
            if (categoriaTest != null) session.remove(session.merge(categoriaTest));
            if (usuarioTest != null) session.remove(session.merge(usuarioTest));

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}