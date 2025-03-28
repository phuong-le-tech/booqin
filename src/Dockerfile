FROM openjdk:21-slim

WORKDIR /app

# Copy the specific JAR file
COPY target/booqIn-1.0.0-SNAPSHOT.jar app.jar

# Create necessary directories
RUN mkdir -p /app/data/images /app/src/main/resources /app/src

# Copy pre-populated database
COPY docker/data/database.mv.db /app/data/
COPY docker/data/image /app/data/images

# Copy resources if needed
COPY src/main/resources/novelists.txt /app/src/main/resources/novelists.txt

# Copy the .env file to the expected location
COPY src/.env /app/src/.env

# Expose the port the app runs on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]