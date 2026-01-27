package org.dam2.adp.ecorastro.service;

import org.dam2.adp.ecorastro.DAO.ActividadDAO;
import org.dam2.adp.ecorastro.DAO.HuellaDAO;
import org.dam2.adp.ecorastro.model.Actividad;
import org.dam2.adp.ecorastro.model.Huella;
import org.dam2.adp.ecorastro.model.Usuario;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

/**
 * Servicio que gestiona la lógica de negocio relacionada con las huellas de carbono.
 * <p>
 * Adapta las llamadas a la versión 3.0 de {@link HuellaDAO}, distinguiendo explícitamente
 * entre datos de usuario individual y datos agregados de la comunidad.
 *
 * @author Antonio Delgado Portero
 * @version 3.0 (Adaptado a DAO con Nomenclatura Explícita)
 */
public class HuellaService {

    private final HuellaDAO huellaDAO;
    private final ActividadDAO actividadDAO;

    public HuellaService() {
        this.huellaDAO = new HuellaDAO();
        this.actividadDAO = new ActividadDAO();
    }

    // ==========================================
    // SECCIÓN: GESTIÓN DE ACTIVIDADES
    // ==========================================

    public List<Actividad> getAllActividades() {
        return actividadDAO.getAllActividades();
    }

    // ==========================================
    // SECCIÓN: CRUD HUELLA
    // ==========================================

    public boolean addHuella(Usuario usuario, Actividad actividad, double valorConsumo, LocalDate fecha) {
        if (usuario == null || actividad == null || valorConsumo <= 0 || fecha == null) {
            return false;
        }

        String unidad = actividad.getIdCategoria().getUnidad();
        Instant instant = fecha.atStartOfDay().toInstant(ZoneOffset.UTC);

        Huella huella = new Huella(usuario, actividad, valorConsumo, unidad, instant);
        return huellaDAO.addHuella(huella);
    }

    public boolean updateHuella(Huella huella) {
        if (huella == null || huella.getValor() <= 0 || huella.getFecha() == null) {
            return false;
        }
        return huellaDAO.updateHuella(huella);
    }

    public boolean deleteHuella(Huella huella) {
        if (huella == null) return false;
        return huellaDAO.deleteHuella(huella);
    }

    /**
     * Recupera el historial completo de huellas de un usuario.
     */
    public List<Huella> getHuellasPorUsuario(int idUsuario) {
        // Adaptado al nuevo DAO
        return huellaDAO.getHistorialHuellasUsuario(idUsuario);
    }

    /**
     * Recupera huellas filtradas por rango de fechas.
     */
    public List<Huella> getHuellasPorFecha(int idUsuario, LocalDate fechaInicio, LocalDate fechaFin) {
        // Adaptado al nuevo DAO
        return huellaDAO.getHuellasUsuarioPorRangoFecha(idUsuario, fechaInicio, fechaFin);
    }

    // ==========================================
    // SECCIÓN: CÁLCULOS Y ESTADÍSTICAS (KPIs y Gráficos)
    // ==========================================

    public double calcularImpacto(Huella huella) {
        if (huella == null || huella.getIdActividad() == null) return 0.0;
        return huella.getValor() * huella.getIdActividad().getIdCategoria().getFactorEmision();
    }

    /**
     * KPI GLOBAL: Impacto total histórico de toda la comunidad.
     */
    public double getTotalImpactoComunidad() {
        return huellaDAO.getTotalImpactoComunidad();
    }

    /**
     * KPI PERSONAL: Impacto total de un usuario en un periodo específico.
     */
    public double getTotalImpactoUsuarioFecha(int idUsuario, LocalDate fechaInicio, LocalDate fechaFin) {
        // Adaptado al nuevo DAO
        return huellaDAO.getTotalImpactoUsuarioPorRangoFecha(idUsuario, fechaInicio, fechaFin);
    }

    /**
     * GRÁFICO: Distribución del impacto por categorías para un usuario.
     */
    public Map<String, Double> getImpactoPorCategoriaUsuario(int idUsuario, LocalDate inicio, LocalDate fin) {
        // Adaptado al nuevo DAO
        return huellaDAO.getImpactoUsuarioPorCategoria(idUsuario, inicio, fin);
    }

    /**
     * Obtiene los datos para el gráfico de "Últimos 12 meses".
     * Calcula automáticamente el rango desde hace 11 meses hasta hoy.
     *
     * @param idUsuario ID del usuario.
     * @return Lista de [Año, Mes, Valor].
     */
    public List<Object[]> getEvolucionUltimos12Meses(int idUsuario) {
        LocalDate fin = LocalDate.now();
        // Vamos 11 meses atrás para tener un total de 12 meses (11 pasados + actual)
        LocalDate inicio = fin.minusMonths(11).withDayOfMonth(1);

        return huellaDAO.getEvolucionRangoFechaUsuario(idUsuario, inicio, fin);
    }

    /**
     * ESTADÍSTICA GLOBAL: Media de impacto por categoría (Histórico).
     */
    public Map<String, Double> getMediaImpactoPorCategoria() {
        // Adaptado al nuevo DAO
        return huellaDAO.getMediaImpactoComunidadPorCategoriaHistorico();
    }

    /**
     * ESTADÍSTICA GLOBAL: Media de impacto por categoría (Filtrado por fecha).
     */
    public Map<String, Double> getMediaImpactoPorCategoriaFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        // Adaptado al nuevo DAO
        return huellaDAO.getMediaImpactoComunidadPorCategoriaRangoFecha(fechaInicio, fechaFin);
    }

    // ==========================================
    // SECCIÓN: GAMIFICACIÓN Y CONTEXTO SOCIAL
    // ==========================================

    /**
     * Obtiene el número de usuarios activos para dar contexto al ranking.
     * Ej: "Puesto 5 de [300]".
     */
    public Long getTotalUsuariosActivos() {
        return huellaDAO.countUsuariosActivosComunidad();
    }

    /**
     * Calcula la posición del usuario en el ranking global.
     * Incluye validación para no clasificar usuarios sin huellas (evita el "Falso Top 1").
     */
    public Long getRankingUsuario(int idUsuario) {
        // 1. Verificación: ¿Tiene impacto histórico el usuario?
        double impactoTotal = huellaDAO.getTotalImpactoUsuarioPorRangoFecha(
                idUsuario,
                LocalDate.of(1970, 1, 1),
                LocalDate.of(2100, 1, 1)
        );

        // Si es 0, no participa en el ranking
        if (impactoTotal <= 0.001) {
            return 0L;
        }

        // 2. Obtener ranking real del DAO
        return huellaDAO.getRankingUsuarioEnComunidad(idUsuario);
    }

    /**
     * Calcula la media de emisiones EXCLUYENDO al usuario actual.
     * Esencial para el gráfico comparativo "Yo vs Los Demás".
     *
     * @param idUsuario El usuario a excluir.
     * @return La media real del resto de la comunidad.
     */
    public double getMediaComunidadSinUsuario(int idUsuario) {
        double totalGlobal = huellaDAO.getTotalImpactoComunidad();

        // Reutilizamos el cálculo de impacto histórico del usuario
        double miTotal = huellaDAO.getTotalImpactoUsuarioPorRangoFecha(
                idUsuario,
                LocalDate.of(1970, 1, 1),
                LocalDate.now().plusDays(1)
        );

        long numUsuarios = huellaDAO.countUsuariosActivosComunidad();

        // Evitar división por cero o resultados negativos si soy el único usuario
        if (numUsuarios <= 1) {
            return 0.0;
        }

        // Fórmula: (TotalGlobal - MiAporte) / (TotalPersonas - Yo)
        return (totalGlobal - miTotal) / (numUsuarios - 1);
    }
}