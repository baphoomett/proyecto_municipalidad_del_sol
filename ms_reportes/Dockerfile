# ===== ETAPA 1: BUILD =====
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar proyecto completo
COPY . .

# Compilar el jar
RUN mvn clean package -DskipTests

# ===== ETAPA 2: RUN =====
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el jar generado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]