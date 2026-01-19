package org.dam2.adp.ecorastro.service;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HabitoServiceTest {

    private HabitoService service;

    // Datos de prueba
    private Usuario usuarioTest;
    private Actividad actividadTest;
    private Categoria categoriaTest;
    private Habito habitoGuardado; // Referencia para borrarlo al final

    @BeforeEach
    void setUp() {
        service = new HabitoService();
        prepararBaseDeDatos();
    }

    @AfterEach
    void tearDown() {
        limpiarBaseDeDatos();
    }

    @Test
    void testRegistrarHabitoCorrecto() {
        // 1. Arrange
        int frecuencia = 3;
        String tipo = "Semanal";

        // 2. Act
        boolean resultado = service.addHabito(usuarioTest, actividadTest, frecuencia, tipo);

        // 3. Assert
        assertTrue(resultado, "Debe permitir registrar un hábito con datos válidos");

        // Verificación extra: Consultar si realmente se guardó
        List<Habito> habitos = service.getHabitosByUsuario(usuarioTest.getId());
        assertFalse(habitos.isEmpty());
        assertEquals(1, habitos.size());
        assertEquals("Semanal", habitos.get(0).getTipo());

        // Guardamos referencia para el tearDown
        habitoGuardado = habitos.get(0);
    }

    @Test
    void testValidacionFrecuenciaNegativa() {
        // Intenta registrar una frecuencia negativa (ej: -5 veces al día)
        boolean resultado = service.addHabito(usuarioTest, actividadTest, -5, "Diario");

        assertFalse(resultado, "NO debe guardar hábitos con frecuencia negativa");
    }

    @Test
    void testValidacionDatosNulos() {
        // Intenta registrar sin usuario
        boolean resultado = service.addHabito(null, actividadTest, 1, "Diario");
        assertFalse(resultado, "NO debe guardar si el usuario es null");

        // Intenta registrar sin actividad
        boolean resultado2 = service.addHabito(usuarioTest, null, 1, "Diario");
        assertFalse(resultado2, "NO debe guardar si la actividad es null");
    }

    @Test
    void testEliminarHabito() {
        // 1. Primero lo creamos
        service.addHabito(usuarioTest, actividadTest, 1, "Diario");
        Habito creado = service.getHabitosByUsuario(usuarioTest.getId()).get(0);

        // 2. Lo borramos
        boolean eliminado = service.deleteHabito(creado);

        // 3. Verificamos
        assertTrue(eliminado, "Debe devolver true al eliminar correctamente");
        List<Habito> lista = service.getHabitosByUsuario(usuarioTest.getId());
        assertTrue(lista.isEmpty(), "La lista debe estar vacía después de borrar");

        habitoGuardado = null; // Ya está borrado, no hace falta en tearDown
    }

    // --- MÉTODOS AUXILIARES ---

    private void prepararBaseDeDatos() {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();

            usuarioTest = new Usuario("Test Service Habito", "habito.service@test.com", "1234");
            session.persist(usuarioTest);

            categoriaTest = new Categoria();
            categoriaTest.setNombre("Cat Service Habito");
            categoriaTest.setFactorEmision(BigDecimal.ONE);
            categoriaTest.setUnidad("ud");
            session.persist(categoriaTest);

            actividadTest = new Actividad();
            actividadTest.setNombre("Act Service Habito");
            actividadTest.setIdCategoria(categoriaTest);
            session.persist(actividadTest);

            tx.commit();
        }
    }

    private void limpiarBaseDeDatos() {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();

            // Borramos el hábito si se creó
            if (habitoGuardado != null) {
                // Usamos merge para re-asociarlo a la sesión actual antes de borrar
                session.remove(session.merge(habitoGuardado));
            }
            // Borramos dependencias
            if (actividadTest != null) session.remove(session.merge(actividadTest));
            if (categoriaTest != null) session.remove(session.merge(categoriaTest));
            if (usuarioTest != null) session.remove(session.merge(usuarioTest));

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}