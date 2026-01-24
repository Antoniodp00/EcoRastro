package org.dam2.adp.ecorastro.service;

import org.dam2.adp.ecorastro.DAO.RecomendacionDAO;
import org.dam2.adp.ecorastro.model.Recomendacion;

import java.util.List;
import java.util.Random;

/**
 * Servicio encargado de proporcionar recomendaciones y consejos ecológicos.
 * <p>
 * Selecciona consejos aleatorios basados en categorías específicas para fomentar
 * la concienciación ambiental del usuario.
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class RecomendacionService {

    private final RecomendacionDAO recomendacionDAO = new RecomendacionDAO();
    private final Random random = new Random();

    /**
     * Genera un consejo aleatorio relacionado con una categoría específica.
     * <p>
     * Si no se encuentran consejos para la categoría o esta es nula, devuelve un mensaje genérico.
     *
     * @param categoria El nombre de la categoría sobre la que se busca consejo (ej: "Agua").
     * @return Una cadena de texto con la descripción del consejo.
     */
    public String generarConsejo (String categoria){

     if (categoria == null ||categoria.trim().isEmpty()){
         return "Pequeños gestos cambian el mundo. Intenta reducir tu consumo y reutilizar siempre que puedas";
     }

     List<Recomendacion> recomendaciones = recomendacionDAO.getRecomendacionesPorCategoria(categoria);

     if (recomendaciones != null && !recomendaciones.isEmpty()){
         int indiceAleatorio = random.nextInt(recomendaciones.size());
         return recomendaciones.get(indiceAleatorio).getDescripcion();
     }

        return "Pequeños gestos cambian el mundo. Intenta reducir tu consumo y reutilizar siempre que puedas";
    }
}