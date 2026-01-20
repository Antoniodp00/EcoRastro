package org.dam2.adp.ecorastro.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

/**
 * Entidad que representa un consejo o recomendación ecológica.
 * <p>
 * Mapea la tabla 'recomendacion' del esquema 'ecorastro_db'.
 * Las recomendaciones están vinculadas a una categoría específica para ofrecer consejos contextuales.
 *
 * @author TuNombre
 * @version 1.0
 */
@Entity
@Table(name = "recomendacion", schema = "ecorastro_db")
public class Recomendacion {

    /** Identificador único de la recomendación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recomendacion", nullable = false)
    private Integer id;

    /** Categoría a la que pertenece la recomendación. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria idCategoria;

    /** Texto descriptivo del consejo. */
    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    /** Estimación del impacto positivo si se sigue el consejo (opcional). */
    @Column(name = "impacto_estimado", precision = 10, scale = 2)
    private BigDecimal impactoEstimado;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Categoria getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Categoria idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getImpactoEstimado() {
        return impactoEstimado;
    }

    public void setImpactoEstimado(BigDecimal impactoEstimado) {
        this.impactoEstimado = impactoEstimado;
    }

    @Override
    public String toString() {
        return "Recomendacion{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", impactoEstimado=" + impactoEstimado +
                ", idCategoria=" + (idCategoria != null ? idCategoria.getId() : "null") +
                '}';
    }
}