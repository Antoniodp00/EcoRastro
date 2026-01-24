package org.dam2.adp.ecorastro.service;

import org.dam2.adp.ecorastro.DAO.HabitoDAO;
import org.dam2.adp.ecorastro.DAO.HuellaDAO;
import org.dam2.adp.ecorastro.model.Actividad;
import org.dam2.adp.ecorastro.model.Habito;
import org.dam2.adp.ecorastro.model.HabitoId;
import org.dam2.adp.ecorastro.model.Usuario;

import java.time.Instant;
import java.util.List;

/**
 * Servicio que gestiona la lógica de negocio relacionada con los hábitos de los usuarios.
 * <p>
 * Actúa como intermediario entre los controladores y la capa de acceso a datos (DAO).
 * Permite crear, eliminar y consultar hábitos recurrentes.
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class HabitoService {
    private final HabitoDAO habitoDAO = new HabitoDAO();

    /**
     * Añade un nuevo hábito al sistema.
     * <p>
     * Valida los datos de entrada antes de intentar la inserción.
     *
     * @param usuario    El usuario propietario del hábito.
     * @param actividad  La actividad que se convierte en hábito.
     * @param frecuencia Número de veces que se repite la actividad.
     * @param tipo       Periodicidad (ej: "Diario", "Semanal").
     * @return true si el hábito se creó correctamente, false si hubo algún error o datos inválidos.
     */
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

    /**
     * Elimina un hábito existente.
     *
     * @param habito El hábito a eliminar.
     * @return true si se eliminó correctamente, false en caso contrario.
     */
    public boolean deleteHabito(Habito habito) {
        return habitoDAO.deleteHabito(habito);
    }

    /**
     * Obtiene todos los hábitos registrados por un usuario específico.
     *
     * @param idUsuario El ID del usuario.
     * @return Lista de hábitos del usuario.
     */
    public List<Habito> getHabitosByUsuario(int idUsuario) {
        return habitoDAO.getHabitosByUsuario(idUsuario);
    }

    /**
     * Busca un hábito por su identificador compuesto.
     *
     * @param idHabito El ID compuesto (Usuario + Actividad).
     * @return El hábito encontrado o null si no existe.
     */
    public Habito getHabitoById(HabitoId idHabito) {
        return habitoDAO.getHabitoById(idHabito);
    }

    /**
     * Obtiene el hábito con mayor frecuencia registrado por un usuario.
     * <p>
     * Útil para generar estadísticas o recomendaciones personalizadas.
     *
     * @param idUsuario El ID del usuario.
     * @return El hábito más frecuente o null si no tiene hábitos.
     */
    public Habito getHabitoMasFrecuente(int idUsuario) {
        return habitoDAO.getHabitoMasFrecuente(idUsuario);
    }
}