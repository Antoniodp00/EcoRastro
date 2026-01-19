package org.dam2.adp.ecorastro.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD: Definimos el comportamiento esperado del sistema de recomendaciones.
 */
class RecomendacionServiceTest {

    private RecomendacionService recomendador;

    @BeforeEach
    void setUp() {
        recomendador = new RecomendacionService();
    }

    @Test
    @DisplayName("Debe recomendar LEDs si la categoría es Energía")
    void testRecomendacionEnergia() {
        // Arrange
        String categoria = "Energía";

        // Act
        String consejo = recomendador.generarConsejo(categoria);

        // Assert
        assertNotNull(consejo);
        assertTrue(consejo.contains("LED") || consejo.contains("luces"),
                "El consejo para Energía debería mencionar iluminación eficiente o LEDs");
    }

    @Test
    @DisplayName("Debe recomendar transporte público si la categoría es Transporte")
    void testRecomendacionTransporte() {
        String consejo = recomendador.generarConsejo("Transporte");
        assertTrue(consejo.contains("público") || consejo.contains("bici") || consejo.contains("caminar"),
                "El consejo debería sugerir alternativas al coche");
    }

    @Test
    @DisplayName("Debe dar un consejo genérico para categorías desconocidas")
    void testCategoriaDesconocida() {
        String consejo = recomendador.generarConsejo("Nuclear");
        assertEquals("Intenta reducir tu consumo y reutilizar siempre que puedas.", consejo);
    }
}