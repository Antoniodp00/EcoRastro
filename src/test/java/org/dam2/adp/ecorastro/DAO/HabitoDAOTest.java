package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HabitoDAOTest {

    private HabitoDAO habitoDAO;

    // Datos necesarios para crear un Hábito válido
    private Usuario usuarioTest;
    private Categoria categoriaTest;
    private Actividad actividadTest;
    private Habito habitoGuardado;

    @BeforeEach
    void setUp() {
        habitoDAO = new HabitoDAO();
        crearDatosDePrueba();
    }

    @AfterEach
    void tearDown() {
        limpiarDatosDePrueba();
    }

    @Test
    void testGuardarHabito() {
        // 1. Arrange: Crear el ID compuesto y el Hábito
        HabitoId id = new HabitoId();
        id.setIdUsuario(usuarioTest.getId());
        id.setIdActividad(actividadTest.getId());

        Habito habito = new Habito();
        habito.setId(id);
        habito.setIdUsuario(usuarioTest);
        habito.setIdActividad(actividadTest);
        habito.setFrecuencia(1); // 1 vez al día/semana
        habito.setTipo("Diario");
        habito.setUltimaFecha(Instant.now());

        // 2. Act: Guardar
        boolean resultado = habitoDAO.addHabito(habito);
        if (resultado) habitoGuardado = habito;

        // 3. Assert
        assertTrue(resultado, "El DAO debería devolver true al guardar");

        // Verificar que existe en BBDD recuperándolo
        Habito recuperado = habitoDAO.getHabitoById(id);
        assertNotNull(recuperado, "Deberíamos poder recuperar el hábito recién guardado");
        assertEquals("Diario", recuperado.getTipo());
    }

    @Test
    void testObtenerHabitosPorUsuario() {
        // 1. Arrange: Guardar un hábito primero
        testGuardarHabito();

        // 2. Act
        List<Habito> habitos = habitoDAO.getHabitosByUsuario(usuarioTest.getId());

        // 3. Assert
        assertNotNull(habitos);
        assertFalse(habitos.isEmpty());
        assertEquals(actividadTest.getNombre(), habitos.get(0).getIdActividad().getNombre());
    }

    @Test
    void testEliminarHabito() {
        // 1. Arrange
        testGuardarHabito();
        assertNotNull(habitoGuardado);

        // 2. Act
        boolean eliminado = habitoDAO.deleteHabito(habitoGuardado);

        // 3. Assert
        assertTrue(eliminado);
        assertNull(habitoDAO.getHabitoById(habitoGuardado.getId()), "El hábito no debería existir después de borrarlo");

        habitoGuardado = null; // Evitar que el tearDown intente borrarlo de nuevo
    }

    @Test
    void testGetHabitoMasFrecuente() {
        // 1. Arrange: Crear dos hábitos con distinta frecuencia
        // Hábito 1 (el que ya tenemos, frecuencia 1)
        testGuardarHabito();

        // Hábito 2 (frecuencia 5)
        Actividad actividad2 = crearActividadExtra("Actividad Frecuente");
        Habito habito2 = new Habito(usuarioTest, actividad2, 5, "Diario");
        habitoDAO.addHabito(habito2);

        // 2. Act
        Habito masFrecuente = habitoDAO.getHabitoMasFrecuente(usuarioTest.getId());

        // 3. Assert
        assertNotNull(masFrecuente);
        assertEquals(5, masFrecuente.getFrecuencia());
        assertEquals("Actividad Frecuente", masFrecuente.getIdActividad().getNombre());

        // Limpieza extra
        habitoDAO.deleteHabito(habito2);
        borrarActividadExtra(actividad2);
    }

    // --- UTILS DE CONFIGURACIÓN ---
    private void crearDatosDePrueba() {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();

            usuarioTest = new Usuario("Test Habito", "habito@test.com", "1234");
            session.persist(usuarioTest);

            categoriaTest = new Categoria();
            categoriaTest.setNombre("Cat Habito");
            categoriaTest.setFactorEmision(BigDecimal.ONE);
            categoriaTest.setUnidad("ud");
            session.persist(categoriaTest);

            actividadTest = new Actividad();
            actividadTest.setNombre("Actividad Habito");
            actividadTest.setIdCategoria(categoriaTest);
            session.persist(actividadTest);

            tx.commit();
        }
    }

    private Actividad crearActividadExtra(String nombre) {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            Actividad a = new Actividad();
            a.setNombre(nombre);
            a.setIdCategoria(categoriaTest); // Reusamos la categoría
            session.persist(a);
            tx.commit();
            return a;
        }
    }

    private void borrarActividadExtra(Actividad a) {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();
            session.remove(session.merge(a));
            tx.commit();
        }
    }

    private void limpiarDatosDePrueba() {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();

            if (habitoGuardado != null) session.remove(session.merge(habitoGuardado));
            if (actividadTest != null) session.remove(session.merge(actividadTest));
            if (categoriaTest != null) session.remove(session.merge(categoriaTest));
            if (usuarioTest != null) session.remove(session.merge(usuarioTest));

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}