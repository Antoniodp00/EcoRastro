package org.dam2.adp.ecorastro.service;

import org.dam2.adp.ecorastro.DAO.HabitoDAO;
import org.dam2.adp.ecorastro.DAO.HuellaDAO;
import org.dam2.adp.ecorastro.model.Actividad;
import org.dam2.adp.ecorastro.model.Habito;
import org.dam2.adp.ecorastro.model.HabitoId;
import org.dam2.adp.ecorastro.model.Usuario;

import java.time.Instant;
import java.util.List;

public class HabitoService {
    private final HabitoDAO habitoDAO = new HabitoDAO();

    public boolean addHabito(Usuario usuario, Actividad actividad, int frecuencia, String tipo) {
        boolean insertado;
        if (usuario == null || actividad == null || frecuencia < 0 || tipo == null) {
            insertado = false;
        } else {
            Habito habito = new Habito();

            habito.setIdUsuario(usuario);
            habito.setIdActividad(actividad);

            habito.setFrecuencia(frecuencia);
            habito.setTipo(tipo);
            habito.setUltimaFecha(Instant.now());
            insertado = habitoDAO.addHabito(habito);

        }
        return insertado;
    }

    public boolean deleteHabito(Habito habito) {
        return habitoDAO.deleteHabito(habito);
    }

    public List<Habito> getHabitosByUsuario(int idUsuario) {
        return habitoDAO.getHabitosByUsuario(idUsuario);
    }

    public Habito getHabitoById(HabitoId idHabito) {
        return habitoDAO.getHabitoById(idHabito);
    }

    public Habito getHabitoMasFrecuente(int idUsuario) {
        return habitoDAO.getHabitoMasFrecuente(idUsuario);
    }


}
