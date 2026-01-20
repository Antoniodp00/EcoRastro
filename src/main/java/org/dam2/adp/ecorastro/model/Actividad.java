package org.dam2.adp.ecorastro.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una actividad ecológica o contaminante.
 * <p>
 * Mapea la tabla 'actividad' del esquema 'ecorastro_db'.
 * Las actividades pertenecen a una {@link Categoria} y pueden ser registradas
 * como huellas o hábitos por los usuarios.
 *
 * @author TuNombre
 * @version 1.0
 */
@Entity
@Table(name = "actividad", schema = "ecorastro_db")
public class Actividad {

    /** Identificador único de la actividad. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad", nullable = false)
    private Integer id;

    /** Nombre descriptivo de la actividad (ej: "Viaje en coche", "Reciclaje de vidrio"). */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /** Categoría a la que pertenece la actividad. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria idCategoria;

    /** Lista de hábitos que referencian esta actividad. */
    @OneToMany(mappedBy = "idActividad")
    private List<Habito> habitos = new ArrayList<>() {
    };

    /** Lista de huellas registradas con esta actividad. */
    @OneToMany(mappedBy = "idActividad")
    private List<Huella> huellas = new ArrayList<>();

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

    public Categoria getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Categoria idCategoria) {
        this.idCategoria = idCategoria;
    }

    public List<Habito> getHabitos() {
        return habitos;
    }

    public void setHabitos(List<Habito> habitos) {
        this.habitos = habitos;
    }

    public List<Huella> getHuellas() {
        return huellas;
    }

    public void setHuellas(List<Huella> huellas) {
        this.huellas = huellas;
    }

    @Override
    public String toString() {
        return "Actividad{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", idCategoria=" + (idCategoria != null ? idCategoria.getId() : "null") +
                '}';
    }
}