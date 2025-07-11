FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY PixelApi/ ./PixelApi/
WORKDIR /app/PixelApi
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/PixelApi/target/PixelApi-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
