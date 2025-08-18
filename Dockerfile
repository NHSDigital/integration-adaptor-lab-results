FROM gradle:jdk21-jammy AS build
COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN ./gradlew --build-cache bootJar

FROM eclipse-temurin:21-jre-jammy

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/integration-adaptor-lab-results.jar /app/integration-adaptor-lab-results.jar

ENTRYPOINT ["java", "-cp", "/app/integration-adaptor-lab-results.jar", "-Dloader.main=uk.nhs.digital.nhsconnect.lab.results.IntegrationAdapterLabResultsApplication", "org.springframework.boot.loader.launch.PropertiesLauncher"]
