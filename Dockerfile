# Build stage
FROM azul/zulu-openjdk:21 AS build
WORKDIR /project

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY app/build.gradle.kts .
COPY settings.gradle.kts .

# Copy source code
COPY app app

# Build the application
RUN ./gradlew build -x test

# Runtime stage
FROM azul/zulu-openjdk:21-jre
WORKDIR /app

# Copy the built artifact from the build stage
COPY --from=build /project/app/build/libs/*.jar app.jar
COPY --from=build /project/app/src/main/resources/application.properties application.properties
# Set the startup command to execute the jar
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.config.location=/app/application.properties"]
