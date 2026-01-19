# ----------------- BUILD FROM SOURCE (DEFAULT) -----------------
# This is the recommended, professional approach
# Builds the JAR inside Docker. First build may take several minutes
# -------- BUILD STAGE --------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests


# -------- RUNTIME STAGE --------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/mtls-server-*.jar app.jar
COPY certs ./certs
EXPOSE 8443
ENV JAVA_OPTS="-Djava.net.preferIPv4Stack=true"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]


# ----------------- OPTIONAL: USE LOCAL JAR -----------------
# If you want faster startup skip Maven download, comment out
# the multi-stage build above and use this instead
# 1. Build JAR locally: mvn clean package -DskipTests
# 2. Uncomment below, comment out multi-stage above

# FROM eclipse-temurin:17-jre-jammy
# WORKDIR /app
# COPY target/mtls-server-0.0.1-SNAPSHOT.jar app.jar
# COPY certs ./certs
# EXPOSE 8443
# ENV JAVA_OPTS="-Djava.net.preferIPv4Stack=true"
# ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
