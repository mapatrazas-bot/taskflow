package com.taskflow.repository;

import com.taskflow.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de Spring Data JPA para usuarios.
 *
 * <p><b>Concepto Spring Data — Repositorio básico:</b>
 * hereda de JpaRepository todos los métodos CRUD sin implementación manual.
 * Solo necesitamos declarar los métodos específicos de nuestro dominio.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /** Buscar usuario por email (único en la BD). */
    Optional<UserEntity> findByEmail(String email);

    /** Verificar si existe un usuario con ese email. */
    boolean existsByEmail(String email);
}