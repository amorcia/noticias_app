# Le dice a Docker que use una imagen base que ya tenga Java 21
FROM eclipse-temurin:21-jre-jammy

# Copia el JAR del backend desde su propia carpeta target
COPY target/*.jar app.jar

# Expone el puerto que usa tu Backend
EXPOSE 8080

# El comando para arrancar tu Backend cuando el contenedor inicie
ENTRYPOINT ["java", "-jar", "/app.jar"]