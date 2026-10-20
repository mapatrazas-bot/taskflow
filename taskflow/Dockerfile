# ==============================================================================
# ETAPA 1: Compilación y Empaquetado (Build Stage)
# ==============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar el wrapper y descriptor de dependencias primero (aprovecha la caché de Docker)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Dar permisos de ejecución al wrapper
RUN chmod +x ./mvnw

# Descargar dependencias en una capa separada
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente y compilar omitiendo tests (los tests se ejecutan en CI)
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ==============================================================================
# ETAPA 2: Entorno de Ejecución Ligero (Runtime Stage)
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Crear un usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar el JAR generado desde la etapa builder
COPY --from=builder /app/target/taskflow-*.jar app.jar

# Asignar permisos al usuario no-root
RUN chown -R appuser:appgroup /app
USER appuser

# Exponer el puerto estándar
EXPOSE 8080

# Parámetros de la JVM y arranque de la aplicación
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]