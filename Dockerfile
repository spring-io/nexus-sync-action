FROM gradle:8.14-jdk17-alpine AS build
COPY . /app/
RUN cd /app && gradle -Dorg.gradle.welcome=never --no-daemon :action:bootJar

FROM ghcr.io/bell-sw/liberica-openjre-debian:17
COPY --from=build /app/action/build/libs/action.jar /opt/action/action.jar
ENTRYPOINT ["java", "-jar", "/opt/action/action.jar"]
