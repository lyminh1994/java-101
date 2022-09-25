FROM eclipse-temurin:11-alpine
RUN mkdir /opt/app
COPY target/account-service-0.0.1-SNAPSHOT.jar /opt/app
CMD ["java", "-jar", "/opt/app/account-service-0.0.1-SNAPSHOT.jar"]