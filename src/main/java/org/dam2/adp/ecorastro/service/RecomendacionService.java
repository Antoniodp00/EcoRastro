package org.dam2.adp.ecorastro.service;

import org.dam2.adp.ecorastro.DAO.RecomendacionDAO;
import org.dam2.adp.ecorastro.model.Recomendacion;

import java.util.List;
import java.util.Random;

public class RecomendacionService {

    private final RecomendacionDAO recomendacionDAO = new RecomendacionDAO();
    private final Random random = new Random();


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
