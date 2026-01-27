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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para HuellaDAO.
 * Valida tanto las operaciones CRUD como las consultas analíticas complejas
 * (KPIs, Gráficos y Rankings).
 */
class HuellaDAOTest {

    private HuellaDAO huellaDAO;

    // Datos de prueba persistentes
    private Usuario usuarioTest;
    private Usuario usuarioVecinoTest; // Segundo usuario para pruebas de comunidad
    private Categoria categoriaTest;
    private Actividad actividadTest;

    // Referencia rápida para borrar huellas creadas en los tests
    private Huella huellaGuardada;

    @BeforeEach
    void setUp() {
        huellaDAO = new HuellaDAO();
        crearDatosDePrueba();
    }

    @AfterEach
    void tearDown() {
        limpiarDatosDePrueba();
    }

    // ==========================================
    // TESTS: CRUD BÁSICO
    // ==========================================

    @Test
    void testGuardarHuella() {
        // Arrange
        Huella huella = new Huella();
        huella.setIdUsuario(usuarioTest);
        huella.setIdActividad(actividadTest);
        huella.setValor(10.5);
        huella.setUnidad("km");
        huella.setFecha(Instant.now());

        // Act
        boolean resultado = huellaDAO.addHuella(huella);
        if (resultado) huellaGuardada = huella;

        // Assert
        assertTrue(resultado, "El método addHuella debe devolver true");
        assertNotNull(huella.getId(), "La huella debe tener un ID generado");
    }

    @Test
    void testUpdateHuella() {
        // Arrange
        crearHuellaAuxiliar(usuarioTest, 10.0, Instant.now());

        // Act
        huellaGuardada.setValor(99.99);
        boolean actualizado = huellaDAO.updateHuella(huellaGuardada);

        // Assert
        assertTrue(actualizado);
        try (Session s = Connection.getInstance().getSession()) {
            Huella recuperada = s.get(Huella.class, huellaGuardada.getId());
            assertEquals(99.99, recuperada.getValor(), 0.001);
        }
    }

    @Test
    void testDeleteHuella() {
        // Arrange
        crearHuellaAuxiliar(usuarioTest, 10.0, Instant.now());

        // Act
        boolean eliminado = huellaDAO.deleteHuella(huellaGuardada);

        // Assert
        assertTrue(eliminado);
        try (Session s = Connection.getInstance().getSession()) {
            Huella recuperada = s.get(Huella.class, huellaGuardada.getId());
            assertNull(recuperada, "La huella no debería existir tras borrarse");
        }
        huellaGuardada = null; // Evitar que tearDown intente borrarla de nuevo
    }

    // ==========================================
    // TESTS: CONSULTAS DE USUARIO (INDIVIDUAL)
    // ==========================================

    @Test
    void testGetHistorialHuellasUsuario() {
        // Arrange
        crearHuellaAuxiliar(usuarioTest, 10.0, Instant.now());

        // Act
        List<Huella> historial = huellaDAO.getHistorialHuellasUsuario(usuarioTest.getId());

        // Assert
        assertFalse(historial.isEmpty());
        assertEquals(1, historial.size());
        assertEquals(usuarioTest.getId(), historial.get(0).getIdUsuario().getId());
    }

    @Test
    void testGetHuellasUsuarioPorRangoFecha() {
        // Arrange
        Instant hoy = Instant.now();
        crearHuellaAuxiliar(usuarioTest, 10.0, hoy);

        // Act
        List<Huella> resultados = huellaDAO.getHuellasUsuarioPorRangoFecha(
                usuarioTest.getId(),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
        );

        // Assert
        assertFalse(resultados.isEmpty());
        assertEquals(huellaGuardada.getId(), resultados.get(0).getId());
    }

    @Test
    void testGetTotalImpactoUsuarioPorRangoFecha() {
        // Arrange: Valor 20 * Factor 0.5 = Impacto 10.0
        crearHuellaAuxiliar(usuarioTest, 20.0, Instant.now());

        // Act
        double total = huellaDAO.getTotalImpactoUsuarioPorRangoFecha(
                usuarioTest.getId(),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
        );

        // Assert
        assertEquals(10.0, total, 0.001);
    }

    @Test
    void testGetImpactoUsuarioPorCategoria() {
        // Arrange
        crearHuellaAuxiliar(usuarioTest, 10.0, Instant.now()); // 10 * 0.5 = 5.0
        // Creamos una segunda huella manualmente para sumar
        Huella h2 = new Huella();
        h2.setIdUsuario(usuarioTest);
        h2.setIdActividad(actividadTest);
        h2.setValor(5.0); // 5 * 0.5 = 2.5
        h2.setUnidad("km");
        h2.setFecha(Instant.now());
        huellaDAO.addHuella(h2);

        // Act
        Map<String, Double> mapa = huellaDAO.getImpactoUsuarioPorCategoria(
                usuarioTest.getId(),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
        );

        // Assert
        assertTrue(mapa.containsKey("Categoria Test"));
        assertEquals(7.5, mapa.get("Categoria Test"), 0.001); // 5.0 + 2.5

        // Cleanup extra
        huellaDAO.deleteHuella(h2);
    }

    @Test
    void testGetEvolucionMensualUsuario() {
        // Arrange
        int year = LocalDate.now().getYear();

        // Enero: 10 * 0.5 = 5.0
        Instant fechaEnero = LocalDate.of(year, 1, 15).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Huella h1 = crearHuellaManual(usuarioTest, 10.0, fechaEnero);

        // Febrero: 20 * 0.5 = 10.0
        Instant fechaFeb = LocalDate.of(year, 2, 10).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Huella h2 = crearHuellaManual(usuarioTest, 20.0, fechaFeb);

        // Act
        List<Object[]> evolucion = huellaDAO.getEvolucionMensualUsuario(usuarioTest.getId(), year);

        // Assert
        assertNotNull(evolucion);
        assertEquals(2, evolucion.size());

        // Verificar Enero
        assertEquals(1, evolucion.get(0)[0]); // Mes
        assertEquals(5.0, (Double) evolucion.get(0)[2], 0.001); // Total

        // Cleanup
        huellaDAO.deleteHuella(h1);
        huellaDAO.deleteHuella(h2);
    }

    // ==========================================
    // TESTS: CONSULTAS DE COMUNIDAD (GLOBAL)
    // ==========================================

    @Test
    void testGetTotalImpactoComunidad() {
        // Arrange: Usuario 1 (10.0 impacto) + Usuario 2 (5.0 impacto)
        crearHuellaAuxiliar(usuarioTest, 20.0, Instant.now()); // 20*0.5 = 10

        Huella hVecino = crearHuellaManual(usuarioVecinoTest, 10.0, Instant.now()); // 10*0.5 = 5

        // Act
        double totalComunidad = huellaDAO.getTotalImpactoComunidad();

        // Assert
        // Nota: En un entorno real puede haber más datos, pero aseguramos que SUME al menos estos
        assertTrue(totalComunidad >= 15.0);

        // Cleanup
        huellaDAO.deleteHuella(hVecino);
    }

    @Test
    void testCountUsuariosActivosComunidad() {
        // Arrange
        crearHuellaAuxiliar(usuarioTest, 10.0, Instant.now());
        Huella hVecino = crearHuellaManual(usuarioVecinoTest, 10.0, Instant.now());

        // Act
        Long usuariosActivos = huellaDAO.countUsuariosActivosComunidad();

        // Assert
        assertTrue(usuariosActivos >= 2, "Debería haber al menos 2 usuarios activos");

        huellaDAO.deleteHuella(hVecino);
    }

    @Test
    void testGetRankingUsuarioEnComunidad() {
        // Arrange
        // Usuario Test: Impacto 100 (Muy alto -> Ranking malo)
        crearHuellaAuxiliar(usuarioTest, 200.0, Instant.now()); // 200*0.5 = 100

        // Vecino: Impacto 5 (Muy bajo -> Ranking bueno)
        Huella hVecino = crearHuellaManual(usuarioVecinoTest, 10.0, Instant.now()); // 10*0.5 = 5

        // Act
        Long rankUsuario = huellaDAO.getRankingUsuarioEnComunidad(usuarioTest.getId());
        Long rankVecino = huellaDAO.getRankingUsuarioEnComunidad(usuarioVecinoTest.getId());

        // Assert
        // El vecino (5) es mejor que el usuario (100).
        // Si solo hay estos 2: Vecino=1, Usuario=2
        assertTrue(rankVecino < rankUsuario, "El vecino con menos impacto debería tener mejor ranking (número menor)");

        huellaDAO.deleteHuella(hVecino);
    }

    @Test
    void testGetMediaImpactoComunidadPorCategoriaHistorico() {
        // Arrange
        crearHuellaAuxiliar(usuarioTest, 10.0, Instant.now());

        // Act
        Map<String, Double> medias = huellaDAO.getMediaImpactoComunidadPorCategoriaHistorico();

        // Assert
        assertTrue(medias.containsKey("Categoria Test"));
        assertNotNull(medias.get("Categoria Test"));
    }

    // ==========================================
    // HELPERS (UTILIDADES DE PRUEBA)
    // ==========================================

    private void crearHuellaAuxiliar(Usuario u, double valor, Instant fecha) {
        Huella h = new Huella();
        h.setIdUsuario(u);
        h.setIdActividad(actividadTest);
        h.setValor(valor);
        h.setUnidad("km");
        h.setFecha(fecha);
        huellaDAO.addHuella(h);
        huellaGuardada = h;
    }

    private Huella crearHuellaManual(Usuario u, double valor, Instant fecha) {
        Huella h = new Huella();
        h.setIdUsuario(u);
        h.setIdActividad(actividadTest);
        h.setValor(valor);
        h.setUnidad("km");
        h.setFecha(fecha);
        huellaDAO.addHuella(h);
        return h;
    }

    private void crearDatosDePrueba() {
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();

            // 1. Usuarios
            usuarioTest = new Usuario("TestUser", "test@huella.com", "1234");
            session.persist(usuarioTest);

            usuarioVecinoTest = new Usuario("VecinoUser", "vecino@huella.com", "1234");
            session.persist(usuarioVecinoTest);

            // 2. Categoría y Actividad
            categoriaTest = new Categoria();
            categoriaTest.setNombre("Categoria Test");
            categoriaTest.setFactorEmision(0.5); // Factor fácil para cálculos mentales
            categoriaTest.setUnidad("km");
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

            // 1. Borrar Huellas de los usuarios de prueba
            session.createQuery("DELETE FROM Huella h WHERE h.idUsuario.id IN (:uid1, :uid2)")
                    .setParameter("uid1", usuarioTest.getId())
                    .setParameter("uid2", usuarioVecinoTest.getId())
                    .executeUpdate();

            // 2. Borrar Entidades base (Orden inverso a creación)
            if (actividadTest != null) session.remove(session.merge(actividadTest));
            if (categoriaTest != null) session.remove(session.merge(categoriaTest));
            if (usuarioTest != null) session.remove(session.merge(usuarioTest));
            if (usuarioVecinoTest != null) session.remove(session.merge(usuarioVecinoTest));

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}