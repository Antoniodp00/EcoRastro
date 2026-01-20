package org.dam2.adp.ecorastro.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad que representa un registro de huella de carbono en la base de datos.
 * <p>
 * Mapea la tabla 'huella' del esquema 'ecorastro_db'.
 * Almacena la información de una actividad realizada por un usuario en una fecha concreta,
 * incluyendo el valor consumido y la unidad de medida.
 *
 * @author TuNombre
 * @version 1.0
 */
@Entity
@Table(name = "huella", schema = "ecorastro_db")
public class Huella {

    /** Identificador único del registro de huella. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro", nullable = false)
    private Integer id;

    /** Usuario que realizó la actividad. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario idUsuario;

    /** Actividad realizada (ej: Conducir, Reciclar). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_actividad", nullable = false)
    private Actividad idActividad;

    /** Valor numérico del consumo o actividad (ej: 100). */
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    /** Unidad de medida asociada al valor (ej: km, kWh). */
    @Column(name = "unidad", nullable = false, length = 20)
    private String unidad;

    /** Fecha y hora en la que se registró la huella. */
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fecha")
    private Instant fecha;

    /**
     * Constructor vacío requerido por JPA/Hibernate.
     */
    public Huella() {
    }

    /**
     * Constructor completo para crear una nueva instancia de Huella.
     *
     * @param idUsuario   El usuario asociado al registro.
     * @param idActividad La actividad realizada.
     * @param valor       El valor numérico del consumo o actividad.
     * @param unidad      La unidad de medida (ej. km, kg, kWh).
     * @param fecha       La fecha y hora del registro.
     */
    public Huella(Usuario idUsuario, Actividad idActividad, BigDecimal valor, String unidad, Instant fecha) {
        this.idUsuario = idUsuario;
        this.idActividad = idActividad;
        this.valor = valor;
        this.unidad = unidad;
        this.fecha = fecha;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "Huella{" +
                "id=" + id +
                ", valor=" + valor +
                ", unidad='" + unidad + '\'' +
                ", fecha=" + fecha +
                ", idUsuario=" + (idUsuario != null ? idUsuario.getId() : "null") +
                ", idActividad=" + (idActividad != null ? idActividad.getId() : "null") +
                '}';
    }
}