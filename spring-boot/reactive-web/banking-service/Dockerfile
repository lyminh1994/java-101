FROM eclipse-temurin:11-alpine
RUN mkdir /opt/app
COPY target/banking-service-0.0.1-SNAPSHOT.jar /opt/app
CMD ["java", "-jar", "/opt/app/banking-service-0.0.1-SNAPSHOT.jar"]