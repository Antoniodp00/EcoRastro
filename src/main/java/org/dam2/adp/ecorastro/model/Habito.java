package org.dam2.adp.ecorastro.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * Entidad que representa un hábito recurrente de un usuario.
 * <p>
 * Mapea la tabla 'habito' del esquema 'ecorastro_db'.
 * Utiliza una clave compuesta {@link HabitoId} formada por el ID del usuario y el ID de la actividad.
 *
 * @author TuNombre
 * @version 1.0
 */
@Entity
@Table(name = "habito", schema = "ecorastro_db")
public class Habito {

    /** Clave primaria compuesta (Usuario + Actividad). */
    @EmbeddedId
    private HabitoId id;

    /** Usuario propietario del hábito. Parte de la clave compuesta. */
    @MapsId("idUsuario")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario idUsuario;

    /** Actividad asociada al hábito. Parte de la clave compuesta. */
    @MapsId("idActividad")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_actividad", nullable = false)
    private Actividad idActividad;

    /** Frecuencia con la que se realiza el hábito (ej: 3 veces). */
    @Column(name = "frecuencia", nullable = false)
    private Integer frecuencia;

    /** Tipo de periodicidad (ej: "Diario", "Semanal", "Mensual"). */
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    /** Fecha de la última actualización o registro del hábito. */
    @Column(name = "ultima_fecha")
    private Instant ultimaFecha;

    /**
     * Constructor vacío requerido por JPA.
     */
    public Habito() {
        this.id = new HabitoId();
    }

    /**
     * Constructor completo para crear un nuevo hábito.
     *
     * @param usuario   El usuario que crea el hábito.
     * @param actividad La actividad que se convierte en hábito.
     * @param frecuencia Número de veces que se repite.
     * @param tipo      Periodicidad (Diario, Semanal, etc.).
     */
    public Habito(Usuario usuario, Actividad actividad, int frecuencia, String tipo) {
        this.idUsuario = usuario;
        this.idActividad = actividad;
        this.frecuencia = frecuencia;
        this.tipo = tipo;
        this.ultimaFecha = Instant.now();
        // Inicializamos la clave compuesta
        this.id = new HabitoId();
        this.id.setIdUsuario(usuario.getId());
        this.id.setIdActividad(actividad.getId());
    }

    public HabitoId getId() {
        return id;
    }

    public void setId(HabitoId id) {
        this.id = id;
    }

    public Usuario getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Usuario idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Actividad getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(Actividad idActividad) {
        this.idActividad = idActividad;
    }

    public Integer getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(Integer frecuencia) {
        this.frecuencia = frecuencia;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Instant getUltimaFecha() {
        return ultimaFecha;
    }

    public void setUltimaFecha(Instant ultimaFecha) {
        this.ultimaFecha = ultimaFecha;
    }
}