package com.taskflow.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Entidad JPA que mapea la tabla users.
 * Implementa UserDetails para integrarse con Spring Security.
 *
 * Concepto UserDetails: Spring Security necesita un objeto que
 * represente al usuario autenticado. Al implementar esta interfaz,
 * nuestra entidad se convierte directamente en el principal de seguridad.
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_gen")
    @SequenceGenerator(name = "users_gen", sequenceName = "users_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Hash BCrypt de la contraseña — nunca se guarda en texto plano */
    @Column(nullable = false)
    private String password;

    /**
     * Rol del usuario: USER o ADMIN.
     * @Enumerated(STRING) guarda "USER" o "ADMIN" en la BD,
     * no índices numéricos (más legible y seguro ante reordenamientos).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol = Rol.USER;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "usuario",
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY)
    private List<TaskEntity> tareas = new ArrayList<>();

    // ── Enum de roles ────────────────────────────────────────────────
    public enum Rol {
        USER, ADMIN
    }

    // ── Constructor vacío requerido por JPA ──────────────────────────
    public UserEntity() {}

    public UserEntity(String nombre, String email, String password, Rol rol) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    // ── UserDetails — métodos requeridos por Spring Security ─────────

    /**
     * Devuelve los roles del usuario como GrantedAuthority.
     * Spring Security usa el prefijo "ROLE_" internamente.
     * Ejemplo: Rol.ADMIN → "ROLE_ADMIN"
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    /** Spring Security usa getUsername() para identificar al usuario */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /** Las 3 flags de cuenta las dejamos en true para simplificar */
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()              { return true; }

    // ── Getters y Setters ─────────────────────────────────────────────
    public Long getId()                        { return id; }
    public String getNombre()                  { return nombre; }
    public void setNombre(String nombre)       { this.nombre = nombre; }
    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }
    public void setPassword(String password)   { this.password = password; }
    public Rol getRol()                        { return rol; }
    public void setRol(Rol rol)               { this.rol = rol; }
    public LocalDateTime getFechaCreacion()    { return fechaCreacion; }
    public LocalDateTime getFechaActualizacion(){ return fechaActualizacion; }
    public List<TaskEntity> getTareas()        { return tareas; }
}