package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Actividad;
import org.dam2.adp.ecorastro.model.Categoria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActividadDAOTest {

    private ActividadDAO actividadDAO;

    // Datos de prueba
    private Categoria categoriaTest;
    private Actividad actividadTest;

    @BeforeEach
    void setUp() {
        actividadDAO = new ActividadDAO();
        crearDatosDePrueba();
    }

    @AfterEach
    void tearDown() {
        limpiarDatosDePrueba();
    }

    @Test
    void testObtenerTodasLasActividades() {
        // 1. Act: Llamamos al DAO
        List<Actividad> lista = actividadDAO.getAllActividades(); // O getAllActividades(), según como lo llames

        // 2. Assert: Verificaciones básicas
        assertNotNull(lista, "La lista no debe ser nula");
        assertFalse(lista.isEmpty(), "La lista debería tener al menos la actividad que acabamos de crear");

        // 3. Assert: Verificación de Integridad (JOIN FETCH)
        // Buscamos nuestra actividad de prueba
        Actividad recuperada = lista.stream()
                .filter(a -> a.getNombre().equals("Actividad DAO Test"))
                .findFirst()
                .orElse(null);

        assertNotNull(recuperada, "Deberíamos encontrar la actividad de prueba");

        // PRUEBA DE FUEGO: Acceder a la categoría sin sesión abierta
        // Si el DAO no hizo JOIN FETCH, esto podría fallar o devolver null si la sesión ya cerró
        assertNotNull(recuperada.getIdCategoria(), "La categoría no debe ser nula");
        assertEquals("Unidad Test", recuperada.getIdCategoria().getUnidad(),
                "Debe poder leerse la unidad de la categoría asociada");
    }

    // --- MÉTODOS AUXILIARES ---

    private void crearDatosDePrueba() {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();

            // 1. Crear Categoría
            categoriaTest = new Categoria();
            categoriaTest.setNombre("Cat DAO Test");
            categoriaTest.setFactorEmision(new BigDecimal("0.123"));
            categoriaTest.setUnidad("Unidad Test");
            session.persist(categoriaTest);

            // 2. Crear Actividad asociada
            actividadTest = new Actividad();
            actividadTest.setNombre("Actividad DAO Test");
            actividadTest.setIdCategoria(categoriaTest);
            session.persist(actividadTest);

            tx.commit();
        }
    }

    private void limpiarDatosDePrueba() {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();

            if (actividadTest != null) session.remove(session.merge(actividadTest));
            if (categoriaTest != null) session.remove(session.merge(categoriaTest));

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}