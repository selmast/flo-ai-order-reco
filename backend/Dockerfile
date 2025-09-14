# --- build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn -q -DskipTests package

# --- runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar --server.port=8081 --server.servlet.context-path=/"]
