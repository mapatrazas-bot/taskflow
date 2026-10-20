# 📋 TaskFlow — Enterprise Task Management REST API

![Java 21](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-blue?logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Multi--stage-2496ED?logo=docker)
![JUnit 5](https://img.shields.io/badge/Tests-35%20Passing-success?logo=junit5)

API REST empresarial para la gestión y categorización de tareas, implementada con **Java 21**, **Spring Boot 4.1.1**, **Spring Security (JWT)**, **Spring Data JPA**, **Flyway** y **Docker**.

---

## 🏛️ Arquitectura del Sistema

* **Capa Web**: RESTful Controllers con DTOs inmutables, Bean Validation y OpenAPI 3.
* **Capa de Seguridad**: Stateless JWT Authentication Filter + BCrypt + Roles (`USER`, `ADMIN`).
* **Capa de Negocio**: Services con lógica funcional basada en Java Streams, Optional y Records.
* **Capa de Persistencia**: Spring Data JPA, Hibernate, Criteria API Specifications y migraciones Flyway.
* **Infraestructura**: Contenerización Multi-Stage con Docker y Docker Compose.

---

## 🚀 Puesta en Marcha Rápida (Docker Compose)

### Prerrequisitos
* Docker y Docker Compose instalados.

### Ejecución
```bash
cd taskflow
docker-compose up --build -d