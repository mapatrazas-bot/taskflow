# ==============================================================================
# ETAPA 1: Compilación y Empaquetado (Build Stage)
# ==============================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copiar descriptor de dependencias
COPY pom.xml ./

# Descargar dependencias en capa cacheada
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar omitiendo tests
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================================
# ETAPA 2: Entorno de Ejecución Ligero (Runtime Stage)
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Crear usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar el JAR generado desde la etapa builder
COPY --from=builder /app/target/taskflow-*.jar app.jar

# Permisos al usuario
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]