FROM gradle:8.5-jdk21 as cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY build.gradle /home/gradle/src/
WORKDIR /home/gradle/src
RUN gradle -b build.gradle clean build -i --stacktrace

FROM gradle:8.5-jdk21 AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle --no-daemon -b build.gradle bootJar -i --stacktrace

FROM amazoncorretto:21-alpine

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/integration-adaptor-lab-results.jar

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/integration-adaptor-lab-results.jar"]
