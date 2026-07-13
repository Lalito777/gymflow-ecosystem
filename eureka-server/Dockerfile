FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Render inyecta la variable PORT y espera que la app escuche ahi.
# Localmente (docker-compose) PORT no esta seteada, por eso el fallback es el puerto propio del servicio.
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8761} -jar app.jar"]
