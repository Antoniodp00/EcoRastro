package org.dam2.adp.ecorastro.service;

import org.dam2.adp.ecorastro.DAO.ActividadDAO;
import org.dam2.adp.ecorastro.DAO.HuellaDAO;
import org.dam2.adp.ecorastro.model.Actividad;
import org.dam2.adp.ecorastro.model.Huella;
import org.dam2.adp.ecorastro.model.Usuario;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Servicio que gestiona la lógica de negocio relacionada con las huellas de carbono.
 * <p>
 * Actúa como intermediario entre los controladores y la capa de acceso a datos (DAO).
 * Permite registrar, consultar y eliminar registros de impacto ambiental.
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class HuellaService {
    private final HuellaDAO huellaDAO = new HuellaDAO();
    private final ActividadDAO actividadDAO = new ActividadDAO();

    /**
     * Añade una nueva huella de carbono al sistema.
     * <p>
     * Valida que el consumo sea positivo y que los datos obligatorios estén presentes.
     *
     * @param usuario      El usuario que realiza el registro.
     * @param actividad    La actividad realizada.
     * @param valorConsumo El valor del consumo (cantidad).
     * @param fecha        La fecha en la que se realizó la actividad.
     * @return true si la huella se añadió correctamente, false en caso contrario.
     */
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

    /**
     * Actualiza una huella existente.
     * <p>
     * Valida que el valor sea positivo y la fecha no sea nula.
     *
     * @param huella La huella con los datos modificados.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean updateHuella(Huella huella) {
        boolean actualizado;

        if ( huella.getValor().compareTo(BigDecimal.ZERO) <= 0|| huella.getFecha() == null) {
            actualizado = false;
        } else {
            actualizado = huellaDAO.updateHuella(huella);
        }
        return actualizado;

    }

    /**
     * Elimina una huella existente.
     *
     * @param huella La huella a eliminar.
     * @return true si se eliminó correctamente, false si hubo un error.
     */
    public boolean deleteHuella(Huella huella) {
        return huellaDAO.deleteHuella(huella);
    }

    /**
     * Calcula el impacto de carbono de una huella específica.
     * <p>
     * Multiplica el valor del consumo por el factor de emisión de la categoría asociada.
     *
     * @param huella La huella a calcular.
     * @return El valor del impacto en CO2 (BigDecimal).
     */
    public BigDecimal calcularImpacto(Huella huella){
        return huella.getValor().multiply(huella.getIdActividad().getIdCategoria().getFactorEmision());
    }

    /**
     * Obtiene todas las actividades disponibles en el sistema.
     *
     * @return Lista de actividades.
     */
    public List<Actividad> getAllActividades() {
        return actividadDAO.getAllActividades();
    }

    /**
     * Obtiene todas las huellas registradas por un usuario específico.
     *
     * @param idUsuario El ID del usuario.
     * @return Lista de huellas del usuario.
     */
    public List<Huella> getHuellasPorUsuario(int idUsuario) {
        return huellaDAO.getHuellasPorUsuario(idUsuario);
    }

    /**
     * Obtiene las huellas de un usuario dentro de un rango de fechas.
     *
     * @param idUsuario   El ID del usuario.
     * @param fechaInicio Fecha de inicio del rango.
     * @param fechaFin    Fecha de fin del rango.
     * @return Lista de huellas encontradas en ese periodo.
     */
    public List<Huella> getHuellasPorFecha(int idUsuario, LocalDate fechaInicio, LocalDate fechaFin) {
        return huellaDAO.getHuellasPorFecha(idUsuario, fechaInicio, fechaFin);
    }

    /**
     * Obtiene la media del impacto de carbono agrupada por categoría.
     * <p>
     * Útil para comparar el rendimiento del usuario con la media global de la comunidad.
     *
     * @return Un mapa donde la clave es el nombre de la categoría y el valor es la media de impacto.
     */
    public Map<String, Double> getMediaImpactoPorCategoria() {
        return huellaDAO.getMediaImpactoPorCategoria();
    }

    /**
     * Obtiene la media del impacto de carbono agrupada por categoría dentro de un rango de fechas.
     * <p>
     * Útil para comparar el rendimiento del usuario con la media global de la comunidad en un periodo específico.
     *
     * @param fechaInicio Fecha de inicio del rango.
     * @param fechaFin    Fecha de fin del rango.
     * @return Un mapa donde la clave es el nombre de la categoría y el valor es la media de impacto.
     */
    public Map<String, Double> getMediaImpactoPorCategoriaFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return huellaDAO.getMediaImpactoPorCategoriaFechas(fechaInicio, fechaFin);
    }

    public double getTotalImpactoComunidad(){
        return huellaDAO.getTotalImpactoComunidad();
    }
}