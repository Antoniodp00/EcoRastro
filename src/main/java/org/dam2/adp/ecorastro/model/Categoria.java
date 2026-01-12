package org.dam2.adp.ecorastro.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "categoria", schema = "ecorastro_db")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria", nullable = false)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "factor_emision", nullable = false, precision = 10, scale = 4)
    private BigDecimal factorEmision;

    @Column(name = "unidad", nullable = false, length = 20)
    private String unidad;

    @OneToMany(mappedBy = "idCategoria")
    private List<Actividad> actividads = new ArrayList<>();

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

}