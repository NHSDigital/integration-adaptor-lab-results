FROM gradle:jdk21-jammy AS build
COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN chmod +x ./gradlew && ls -la gradle/gradle-wrapper.jar

RUN ./gradlew --build-cache bootJar

FROM eclipse-temurin:21-jre-jammy

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/integration-adaptor-lab-results.jar

USER 65534

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/integration-adaptor-lab-results.jar"]