package org.dam2.adp.ecorastro.DAO;

import org.dam2.adp.ecorastro.connection.Connection;
import org.dam2.adp.ecorastro.model.Huella;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase de Acceso a Datos (DAO) para la entidad {@link Huella}.
 * <p>
 * Gestiona las operaciones CRUD y consultas analíticas.
 * <b>Nomenclatura Explícita:</b> Se distingue claramente entre consultas de ámbito
 * USUARIO (individual) y ámbito COMUNIDAD (global/agregado).
 *
 * @author Antonio Delgado Portero
 * @version 3.0 (Renombrado explícito)
 */
public class HuellaDAO {

    // ==========================================
    // CONSTANTES HQL (QUERY LANGUAGE)
    // Nomenclatura: HQL + ÁMBITO (USUARIO/COMUNIDAD) + DESCRIPCIÓN
    // ==========================================

    /**
     * [USUARIO] Obtiene huellas de un usuario específico en un rango.
     * Incluye FETCH JOIN para optimización.
     */
    private final String HQL_GET_HUELLAS_USUARIO_RANGO_FECHA =
            "FROM Huella h " +
                    "JOIN FETCH h.idActividad a " +
                    "JOIN FETCH a.idCategoria " +
                    "WHERE h.idUsuario.id = :uid " +
                    "AND h.fecha >= :inicio AND h.fecha <= :fin " +
                    "ORDER BY h.fecha DESC";

    /**
     * [COMUNIDAD] Media global de impacto por categoría (promedio de todos los usuarios).
     */
    private final String HQL_GET_MEDIA_IMPACTO_COMUNIDAD_POR_CATEGORIA =
            "SELECT c.nombre, AVG(h.valor * c.factorEmision) " +
                    "FROM Huella h " +
                    "JOIN h.idActividad a " +
                    "JOIN a.idCategoria c " +
                    "WHERE h.fecha >= :inicio AND h.fecha <= :fin " +
                    "GROUP BY c.nombre";

    /**
     * [COMUNIDAD] Suma total absoluta de emisiones de todos los usuarios (KPI Global).
     */
    private final String HQL_GET_TOTAL_IMPACTO_COMUNIDAD =
            "SELECT SUM(h.valor * c.factorEmision) " +
                    "FROM Huella h JOIN h.idActividad a JOIN a.idCategoria c";

    /**
     * [COMUNIDAD] Cuenta cuántos usuarios distintos tienen huellas registradas.
     */
    private final String HQL_COUNT_USUARIOS_ACTIVOS_COMUNIDAD =
            "SELECT COUNT(DISTINCT h.idUsuario) FROM Huella h";

    /**
     * [USUARIO] Suma total de emisiones de un usuario específico (KPI Personal).
     */
    private final String HQL_GET_TOTAL_IMPACTO_USUARIO_RANGO_FECHA =
            "SELECT SUM(h.valor * c.factorEmision) " +
                    "FROM Huella h JOIN h.idActividad a JOIN a.idCategoria c " +
                    "WHERE h.idUsuario.id = :uid AND h.fecha >= :inicio AND h.fecha <= :fin";

    /**
     * [USUARIO] Suma de emisiones de un usuario agrupada por categoría (para Gráficos).
     */
    private final String HQL_GET_IMPACTO_USUARIO_POR_CATEGORIA =
            "SELECT c.nombre, SUM(h.valor * c.factorEmision) " +
                    "FROM Huella h " +
                    "JOIN h.idActividad a " +
                    "JOIN a.idCategoria c " +
                    "WHERE h.idUsuario.id = :uid " +
                    "AND h.fecha >= :inicio AND h.fecha <= :fin " +
                    "GROUP BY c.nombre";

    /**
     * [USUARIO] Evolución temporal flexible. Agrupa por Año y Mes dentro de un rango.
     * Esencial para gráficas interanuales (ej: "Últimos 12 meses").
     */
    private final String HQL_GET_EVOLUCION_RANGO_USUARIO =
            "SELECT YEAR(h.fecha), MONTH(h.fecha), SUM(h.valor * c.factorEmision) " +
                    "FROM Huella h " +
                    "JOIN h.idActividad a " +
                    "JOIN a.idCategoria c " +
                    "WHERE h.idUsuario.id = :uid " +
                    "AND h.fecha >= :inicio AND h.fecha <= :fin " +
                    "GROUP BY YEAR(h.fecha), MONTH(h.fecha) " +
                    "ORDER BY YEAR(h.fecha) ASC, MONTH(h.fecha) ASC";

    /**
     * [USUARIO vs COMUNIDAD] Calcula la posición del usuario en el ranking global.
     */
    private final String HQL_GET_RANKING_USUARIO_VS_COMUNIDAD =
            "SELECT COUNT(u) + 1 FROM Usuario u WHERE " +
                    "(SELECT SUM(h.valor * c.factorEmision) FROM Huella h " +
                    " JOIN h.idActividad a JOIN a.idCategoria c WHERE h.idUsuario = u) " +
                    "< " +
                    "(SELECT SUM(h2.valor * c2.factorEmision) FROM Huella h2 " +
                    " JOIN h2.idActividad a2 JOIN a2.idCategoria c2 WHERE h2.idUsuario.id = :uid)";

    // ==========================================
    // OPERACIONES CRUD (Create, Update, Delete)
    // ==========================================

    public boolean addHuella(Huella huella) {
        boolean insertada = false;
        Transaction tx = null;
        try (Session session = Connection.getInstance().getSession()) {
            tx = session.beginTransaction();
            session.persist(huella);
            tx.commit();
            insertada = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
        return insertada;
    }

    public boolean updateHuella(Huella huella) {
        boolean actualizada = false;
        Transaction tx = null;
        try (Session session = Connection.getInstance().getSession()) {
            tx = session.beginTransaction();
            session.merge(huella);
            tx.commit();
            actualizada = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
        return actualizada;
    }

    public boolean deleteHuella(Huella huella) {
        boolean eliminada = false;
        Transaction tx = null;
        try (Session session = Connection.getInstance().getSession()) {
            tx = session.beginTransaction();
            session.remove(huella);
            tx.commit();
            eliminada = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
        return eliminada;
    }

    // ==========================================
    // CONSULTAS: ÁMBITO USUARIO (Datos individuales)
    // ==========================================

    /**
     * Recupera el historial completo de un usuario.
     */
    public List<Huella> getHistorialHuellasUsuario(int idUsuario) {
        // Rango fecha seguro para traer todo el historial
        return getHuellasUsuarioPorRangoFecha(idUsuario, LocalDate.of(1970, 1, 1), LocalDate.of(2100, 1, 1));
    }

    /**
     * Recupera las huellas de un usuario filtradas por fecha.
     */
    public List<Huella> getHuellasUsuarioPorRangoFecha(int idUsuario, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = Connection.getInstance().getSession()) {
            Instant inicio = fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant fin = fechaFin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant();

            return session.createQuery(HQL_GET_HUELLAS_USUARIO_RANGO_FECHA, Huella.class)
                    .setParameter("uid", idUsuario)
                    .setParameter("inicio", inicio)
                    .setParameter("fin", fin)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Calcula el impacto total de un usuario en un rango de fechas.
     */
    public double getTotalImpactoUsuarioPorRangoFecha(int idUsuario, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = Connection.getInstance().getSession()) {
            Instant inicio = fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant fin = fechaFin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant();

            Double resultado = session.createQuery(HQL_GET_TOTAL_IMPACTO_USUARIO_RANGO_FECHA, Double.class)
                    .setParameter("uid", idUsuario)
                    .setParameter("inicio", inicio)
                    .setParameter("fin", fin)
                    .getSingleResult();
            return (resultado != null) ? resultado : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Obtiene el desglose por categorías de un usuario (para Gráficos).
     */
    public Map<String, Double> getImpactoUsuarioPorCategoria(int idUsuario, LocalDate inicio, LocalDate fin) {
        Map<String, Double> resultados = new HashMap<>();
        try (Session session = Connection.getInstance().getSession()) {
            Instant inicioInst = inicio.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant finInst = fin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant();

            List<Object[]> filas = session.createQuery(HQL_GET_IMPACTO_USUARIO_POR_CATEGORIA, Object[].class)
                    .setParameter("uid", idUsuario)
                    .setParameter("inicio", inicioInst)
                    .setParameter("fin", finInst)
                    .getResultList();

            for (Object[] fila : filas) {
                resultados.put((String) fila[0], (Double) fila[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultados;
    }

    public List<Object[]> getEvolucionRangoFechaUsuario(int idUsuario, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = Connection.getInstance().getSession()) {
            Instant inicio = fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant();
            // Ajustamos el fin para incluir todo el último día
            Instant fin = fechaFin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant();

            return session.createQuery(HQL_GET_EVOLUCION_RANGO_USUARIO, Object[].class)
                    .setParameter("uid", idUsuario)
                    .setParameter("inicio", inicio)
                    .setParameter("fin", fin)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    /**
     * Obtiene la posición del usuario en el ranking global.
     */
    public Long getRankingUsuarioEnComunidad(int idUsuario) {
        try (Session session = Connection.getInstance().getSession()) {
            Long ranking = session.createQuery(HQL_GET_RANKING_USUARIO_VS_COMUNIDAD, Long.class)
                    .setParameter("uid", idUsuario)
                    .uniqueResult();
            return (ranking != null) ? ranking : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    // ==========================================
    // CONSULTAS: ÁMBITO COMUNIDAD (Datos Globales)
    // ==========================================

    /**
     * Calcula la media histórica global por categoría.
     */
    public Map<String, Double> getMediaImpactoComunidadPorCategoriaHistorico() {
        return getMediaImpactoComunidadPorCategoriaRangoFecha(LocalDate.of(1970, 1, 1), LocalDate.now());
    }

    /**
     * Calcula la media global por categoría en un periodo específico.
     */
    public Map<String, Double> getMediaImpactoComunidadPorCategoriaRangoFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Double> medias = new HashMap<>();
        try (Session session = Connection.getInstance().getSession()) {
            Instant inicio = fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant fin = fechaFin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant();

            List<Object[]> resultados = session.createQuery(HQL_GET_MEDIA_IMPACTO_COMUNIDAD_POR_CATEGORIA, Object[].class)
                    .setParameter("inicio", inicio)
                    .setParameter("fin", fin)
                    .getResultList();

            for (Object[] fila : resultados) {
                medias.put((String) fila[0], (Double) fila[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return medias;
    }

    /**
     * Obtiene el impacto total acumulado de toda la comunidad.
     */
    public double getTotalImpactoComunidad() {
        try (Session session = Connection.getInstance().getSession()) {
            Double resultado = session.createQuery(HQL_GET_TOTAL_IMPACTO_COMUNIDAD, Double.class).getSingleResult();
            return (resultado != null) ? resultado : 0.0;
        }
    }

    /**
     * Cuenta cuántos usuarios activos hay en total en la comunidad.
     * Útil para mostrar "Puesto X de Y".
     */
    public Long countUsuariosActivosComunidad() {
        try (Session session = Connection.getInstance().getSession()) {
            Long count = session.createQuery(HQL_COUNT_USUARIOS_ACTIVOS_COMUNIDAD, Long.class).uniqueResult();
            return (count != null) ? count : 0L;
        }
    }
}