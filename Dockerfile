FROM gradle:8.8-jdk21-jammy AS build
COPY --chown=gradle:gradle src /home/gradle/src

WORKDIR /home/gradle/src

RUN ./gradlew --build-cache bootJar

FROM eclipse-temurin:21-jre-jammy

EXPOSE 8081

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/integration-adaptor-lab-results.jar

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/integration-adaptor-lab-results.jar"]
