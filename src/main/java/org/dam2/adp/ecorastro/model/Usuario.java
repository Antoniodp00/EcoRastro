package org.dam2.adp.ecorastro.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa a un usuario registrado en la aplicación.
 * <p>
 * Mapea la tabla 'usuario' del esquema 'ecorastro_db'.
 * Contiene la información de perfil, credenciales y relaciones con sus datos (huellas y hábitos).
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
@Entity
@Table(name = "usuario", schema = "ecorastro_db")
public class Usuario {

    /** Identificador único del usuario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario", nullable = false)
    private Integer id;

    /** Nombre completo o nombre de usuario. */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /** Correo electrónico (debe ser único). */
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    /** Contraseña cifrada del usuario. */
    @Column(name = "contrasena", nullable = false, length = 60)
    private String contrasena;

    /** Fecha en la que el usuario se registró en la plataforma. */
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fecha_registro")
    private Instant fechaRegistro;

    /** Lista de hábitos asociados al usuario. */
    @OneToMany(mappedBy = "idUsuario")
    private List<Habito> habitos = new ArrayList<>();

    /** Lista de registros de huella de carbono del usuario. */
    @OneToMany(mappedBy = "idUsuario")
    private List<Huella> huellas = new ArrayList<>();

    /**
     * Constructor vacío requerido por JPA.
     */
    public Usuario() {
    }

    /**
     * Constructor para crear un nuevo usuario.
     *
     * @param nombre     Nombre del usuario.
     * @param email      Correo electrónico.
     * @param contrasena Contraseña (ya cifrada o lista para cifrar).
     */
    public Usuario(String nombre, String email, String contrasena) {
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
        this.fechaRegistro = Instant.now();
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Instant fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
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
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }
}