package org.dam2.adp.ecorastro.service;

import org.dam2.adp.ecorastro.DAO.HuellaDAO;
import org.dam2.adp.ecorastro.model.Actividad;
import org.dam2.adp.ecorastro.model.Huella;
import org.dam2.adp.ecorastro.model.Usuario;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

public class HuellaService {
    private final HuellaDAO huellaDAO = new HuellaDAO();

    public boolean addHuella(Usuario usuario, Actividad actividad, double valorConsumo,  LocalDate fecha) {
        boolean insertado;

        if (usuario == null || actividad == null || BigDecimal.valueOf(valorConsumo).compareTo(BigDecimal.ZERO) <= 0|| fecha == null) {
            insertado = false;
        } else {
            String unidad = actividad.getIdCategoria().getUnidad();
            Instant instant = fecha.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
            Huella huella = new Huella(usuario, actividad, BigDecimal.valueOf(valorConsumo), unidad, instant);
            insertado = huellaDAO.addHuella(huella);
        }
        return insertado;
    }

    public BigDecimal calcularImpacto(Huella huella){
        return huella.getValor().multiply(huella.getIdActividad().getIdCategoria().getFactorEmision());
    }


}
