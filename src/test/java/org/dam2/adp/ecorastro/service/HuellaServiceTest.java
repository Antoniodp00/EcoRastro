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
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class HuellaServiceTest {

    private HuellaService service;

    // Datos auxiliares
    private Usuario usuarioTest;
    private Actividad actividadTest;
    private Categoria categoriaTest;
    // (No guardamos la huella aquí porque el servicio se encarga de eso,
    //  pero deberíamos limpiarla si queremos ser estrictos.
    //  Para simplificar, limpiaremos el usuario y por cascada o FK se limpiará lo demás en un entorno real,
    //  o usamos un método de limpieza robusto).

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
        // Si implementaste el método 'calcularImpacto' en el servicio:
        // Fórmula: Valor * FactorEmision
        // Valor = 100, Factor = 0.5 -> Resultado esperado = 50.0

        Huella huellaSimulada = new Huella();
        huellaSimulada.setValor(new BigDecimal("100"));
        huellaSimulada.setIdActividad(actividadTest); // Tiene factor 0.5

        // Asumiendo que creaste este método en el Service como te sugerí
        BigDecimal impacto = service.calcularImpacto(huellaSimulada);

        assertEquals(0, new BigDecimal("50.00").compareTo(impacto),
                "El cálculo debe ser 100 * 0.5 = 50.00");
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
        // Limpiamos la BBDD de los datos creados
        try (Session session = Connection.getInstance().getSession()) {
            Transaction tx = session.beginTransaction();

            // Nota: Si el servicio guardó huellas reales, habría que borrarlas primero
            // Aquí borramos los padres, si falla por FK es porque se creó una huella y habría que borrarla.
            // Para estos tests unitarios básicos asumimos limpieza simple o BBDD de test.

            // Borrar huellas asociadas al usuario de test (Query HQL)
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