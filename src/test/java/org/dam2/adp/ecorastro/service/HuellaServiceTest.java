package org.dam2.adp.ecorastro.service;

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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HuellaServiceTest {

    private HuellaService service;

    // Datos auxiliares
    private Usuario usuarioTest;
    private Actividad actividadTest;
    private Categoria categoriaTest;
    private Huella huellaGuardada;

    @BeforeEach
    void setUp() {
        service = new HuellaService();
        prepararBaseDeDatos();
    }

    @AfterEach
    void tearDown() {
        limpiarBaseDeDatos();
    }

    @Test
    void testRegistrarHuellaCorrecta() {
        // 1. Arrange
        double valorConsumo = 100.0;

        // 2. Act
        boolean resultado = service.addHuella(
                usuarioTest,
                actividadTest,
                valorConsumo,
                LocalDate.now()
        );

        // 3. Assert
        assertTrue(resultado, "Debe permitir registrar una huella con datos válidos");
        
        // Recuperar para limpiar
        List<Huella> lista = service.getHuellasPorUsuario(usuarioTest.getId());
        if (!lista.isEmpty()) huellaGuardada = lista.get(0);
    }

    @Test
    void testValidacionValorNegativo() {
        // Intenta registrar -50 km
        boolean resultado = service.addHuella(
                usuarioTest,
                actividadTest,
                -50.0,
                LocalDate.now()
        );

        assertFalse(resultado, "NO debe guardar huellas con valor negativo");
    }

    @Test
    void testValidacionActividadNula() {
        // Intenta registrar sin actividad
        boolean resultado = service.addHuella(
                usuarioTest,
                null,
                20.0,
                LocalDate.now()
        );

        assertFalse(resultado, "NO debe guardar si la actividad es null");
    }

    @Test
    void testCalculoImpacto() {
        Huella huellaSimulada = new Huella();
        huellaSimulada.setValor(new BigDecimal("100"));
        huellaSimulada.setIdActividad(actividadTest); // Tiene factor 0.5

        BigDecimal impacto = service.calcularImpacto(huellaSimulada);

        assertEquals(0, new BigDecimal("50.0000").compareTo(impacto),
                "El cálculo debe ser 100 * 0.5 = 50.00");
    }

    @Test
    void testUpdateHuella() {
        // 1. Crear huella
        service.addHuella(usuarioTest, actividadTest, 10.0, LocalDate.now());
        huellaGuardada = service.getHuellasPorUsuario(usuarioTest.getId()).get(0);

        // 2. Modificar
        huellaGuardada.setValor(new BigDecimal("20.0"));
        boolean actualizado = service.updateHuella(huellaGuardada);

        // 3. Verificar
        assertTrue(actualizado);
        Huella recuperada = service.getHuellasPorUsuario(usuarioTest.getId()).get(0);
        assertEquals(0, new BigDecimal("20.0").compareTo(recuperada.getValor()));
    }

    @Test
    void testUpdateHuellaInvalida() {
        // 1. Crear huella
        service.addHuella(usuarioTest, actividadTest, 10.0, LocalDate.now());
        huellaGuardada = service.getHuellasPorUsuario(usuarioTest.getId()).get(0);

        // 2. Modificar con valor inválido
        huellaGuardada.setValor(new BigDecimal("-5.0"));
        boolean actualizado = service.updateHuella(huellaGuardada);

        // 3. Verificar que falla
        assertFalse(actualizado);
    }

    @Test
    void testDeleteHuella() {
        service.addHuella(usuarioTest, actividadTest, 10.0, LocalDate.now());
        huellaGuardada = service.getHuellasPorUsuario(usuarioTest.getId()).get(0);

        boolean eliminado = service.deleteHuella(huellaGuardada);

        assertTrue(eliminado);
        assertTrue(service.getHuellasPorUsuario(usuarioTest.getId()).isEmpty());
        huellaGuardada = null;
    }

    @Test
    void testGetAllActividades() {
        List<Actividad> lista = service.getAllActividades();
        assertNotNull(lista);
        assertFalse(lista.isEmpty());
    }

    @Test
    void testGetHuellasPorFecha() {
        service.addHuella(usuarioTest, actividadTest, 10.0, LocalDate.now());
        huellaGuardada = service.getHuellasPorUsuario(usuarioTest.getId()).get(0);

        List<Huella> resultado = service.getHuellasPorFecha(
                usuarioTest.getId(),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
        );

        assertFalse(resultado.isEmpty());
    }

    @Test
    void testGetMediaImpacto() {
        service.addHuella(usuarioTest, actividadTest, 10.0, LocalDate.now());
        huellaGuardada = service.getHuellasPorUsuario(usuarioTest.getId()).get(0);

        Map<String, Double> medias = service.getMediaImpactoPorCategoria();
        assertNotNull(medias);
        assertTrue(medias.containsKey("Cat Service"));
    }

    // --- UTILS ---

    private void prepararBaseDeDatos() {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();

            usuarioTest = new Usuario("Service User", "service@test.com", "pass");
            session.persist(usuarioTest);

            categoriaTest = new Categoria();
            categoriaTest.setNombre("Cat Service");
            categoriaTest.setFactorEmision(new BigDecimal("0.5000")); // Factor conocido para calcular
            categoriaTest.setUnidad("ud");
            session.persist(categoriaTest);

            actividadTest = new Actividad();
            actividadTest.setNombre("Act Service");
            actividadTest.setIdCategoria(categoriaTest);
            session.persist(actividadTest);

            tx.commit();
        }
    }

    private void limpiarBaseDeDatos() {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();

            if (huellaGuardada != null) {
                // Verificar si existe antes de borrar
                if (session.get(Huella.class, huellaGuardada.getId()) != null) {
                    session.remove(session.merge(huellaGuardada));
                }
            }
            
            // Limpieza de seguridad por si falló la referencia
            session.createQuery("DELETE FROM Huella h WHERE h.idUsuario.id = :uid")
                    .setParameter("uid", usuarioTest.getId())
                    .executeUpdate();

            if (actividadTest != null) session.remove(session.merge(actividadTest));
            if (categoriaTest != null) session.remove(session.merge(categoriaTest));
            if (usuarioTest != null) session.remove(session.merge(usuarioTest));

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}