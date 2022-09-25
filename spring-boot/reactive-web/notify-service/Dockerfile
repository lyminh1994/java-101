FROM eclipse-temurin:11-alpine
RUN mkdir /opt/app
COPY target/notify-service-0.0.1-SNAPSHOT.jar /opt/app
CMD ["java", "-jar", "/opt/app/notify-service-0.0.1-SNAPSHOT.jar"]