package org.dam2.adp.ecorastro.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clase que representa la clave primaria compuesta para la entidad {@link Habito}.
 * <p>
 * Un hábito se identifica de forma única por la combinación de:
 * <ul>
 * <li>El ID del usuario que posee el hábito.</li>
 * <li>El ID de la actividad asociada al hábito.</li>
 * </ul>
 * Implementa {@link Serializable} como requisito de JPA para claves compuestas.
 *
 * @author TuNombre
 * @version 1.0
 */
@Embeddable
public class HabitoId implements Serializable {
    private static final long serialVersionUID = -7195614476825220324L;

    /** ID del usuario. */
    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    /** ID de la actividad. */
    @Column(name = "id_actividad", nullable = false)
    private Integer idActividad;

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(Integer idActividad) {
        this.idActividad = idActividad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HabitoId entity = (HabitoId) o;
        return Objects.equals(this.idUsuario, entity.idUsuario) &&
                Objects.equals(this.idActividad, entity.idActividad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, idActividad);
    }
}