package org.dam2.adp.ecorastro.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que agrupa actividades relacionadas (ej: Transporte, Energía, Alimentación).
 * <p>
 * Mapea la tabla 'categoria' del esquema 'ecorastro_db'.
 * Define el factor de emisión base para calcular el impacto de CO2 de las actividades asociadas.
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
@Entity
@Table(name = "categoria", schema = "ecorastro_db")
public class Categoria {

    /** Identificador único de la categoría. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria", nullable = false)
    private Integer id;

    /** Nombre de la categoría (ej: "Transporte"). */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /** Factor de emisión de CO2 por unidad de medida. */
    @Column(name = "factor_emision", nullable = false, precision = 10, scale = 4)
    private BigDecimal factorEmision;

    /** Unidad de medida estándar para esta categoría (ej: "km", "kWh"). */
    @Column(name = "unidad", nullable = false, length = 20)
    private String unidad;

    /** Lista de actividades que pertenecen a esta categoría. */
    @OneToMany(mappedBy = "idCategoria")
    private List<Actividad> actividads = new ArrayList<>();

    /** Lista de recomendaciones asociadas a esta categoría. */
    @OneToMany(mappedBy = "idCategoria")
    private List<Recomendacion> recomendacions = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getFactorEmision() {
        return factorEmision;
    }

    public void setFactorEmision(BigDecimal factorEmision) {
        this.factorEmision = factorEmision;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public List<Actividad> getActividads() {
        return actividads;
    }

    public void setActividads(List<Actividad> actividads) {
        this.actividads = actividads;
    }

    public List<Recomendacion> getRecomendacions() {
        return recomendacions;
    }

    public void setRecomendacions(List<Recomendacion> recomendacions) {
        this.recomendacions = recomendacions;
    }

    @Override
    public String toString() {
        return "Categoria{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", factorEmision=" + factorEmision +
                ", unidad='" + unidad + '\'' +
                '}';
    }
}