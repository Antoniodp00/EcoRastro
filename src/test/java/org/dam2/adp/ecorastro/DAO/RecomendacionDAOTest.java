package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Categoria;
import org.dam2.adp.ecorastro.model.Recomendacion;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecomendacionDAOTest {

    private RecomendacionDAO recomendacionDAO;

    // Datos de prueba
    private Categoria categoriaTest;
    private Recomendacion recomendacionTest;

    @BeforeEach
    void setUp() {
        recomendacionDAO = new RecomendacionDAO();
        crearDatosDePrueba();
    }

    @AfterEach
    void tearDown() {
        limpiarDatosDePrueba();
    }

    @Test
    void testObtenerRecomendacionesPorCategoria() {
        // 1. Act: Buscamos por el nombre de la categoría que creamos en el setUp
        //    (Debe coincidir exactamente con "Cat Reco Test")
        List<Recomendacion> resultados = recomendacionDAO.getRecomendacionesPorCategoria("Cat Reco Test");

        // 2. Assert
        assertNotNull(resultados, "La lista no debe ser nula");
        assertFalse(resultados.isEmpty(), "Debería encontrar al menos una recomendación");

        Recomendacion encontrada = resultados.get(0);
        assertEquals("Usa menos esto para ahorrar", encontrada.getDescripcion());
        // Verificamos que realmente pertenece a la categoría correcta
        assertEquals("Cat Reco Test", encontrada.getIdCategoria().getNombre());
    }

    @Test
    void testCategoriaSinRecomendaciones() {
        // Buscamos una categoría que no existe o no tiene datos
        List<Recomendacion> resultados = recomendacionDAO.getRecomendacionesPorCategoria("Categoria Fantasma");

        assertNotNull(resultados);
        assertTrue(resultados.isEmpty(), "Si no hay recomendaciones, la lista debe estar vacía (no null)");
    }

    // --- MÉTODOS AUXILIARES ---

    private void crearDatosDePrueba() {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();

            // 1. Crear Categoría
            categoriaTest = new Categoria();
            categoriaTest.setNombre("Cat Reco Test");
            categoriaTest.setFactorEmision(new BigDecimal("0.5"));
            categoriaTest.setUnidad("ud");
            session.persist(categoriaTest);

            // 2. Crear Recomendación asociada
            recomendacionTest = new Recomendacion();
            recomendacionTest.setIdCategoria(categoriaTest);
            recomendacionTest.setDescripcion("Usa menos esto para ahorrar");
            recomendacionTest.setImpactoEstimado(new BigDecimal("10.00"));
            session.persist(recomendacionTest);

            tx.commit();
        }
    }

    private void limpiarDatosDePrueba() {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();

            if (recomendacionTest != null) session.remove(session.merge(recomendacionTest));
            if (categoriaTest != null) session.remove(session.merge(categoriaTest));

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}