# ===============================
# Etapa 1: Build con Maven + Java 26
# ===============================
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Copiamos primero pom.xml para aprovechar caché
COPY pom.xml .

# Descarga dependencias
RUN mvn dependency:go-offline -B

# Copiamos código fuente
COPY src ./src

# Compilamos (sin tests)
RUN mvn clean package -DskipTests

# ===============================
# Etapa 2: Runtime liviano
# ===============================
FROM eclipse-temurin:26-jre

WORKDIR /app

# Copiamos jar generado
COPY --from=build /app/target/*.jar app.jar

# Puerto típico Spring Boot
EXPOSE 8080

# Variables opcionales JVM
ENV JAVA_OPTS=""

# Arranque
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
