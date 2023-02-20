FROM eclipse-temurin:17-jdk-alpine as build

WORKDIR /build
COPY . .

RUN ./gradlew bootJar

FROM eclipse-temurin:17-jdk-alpine as runtime

WORKDIR /app
COPY --from=build /build/build/libs/*.jar /app/app.jar
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.4.8/applicationinsights-agent-3.4.8.jar /app/applicationinsights-agent.jar

EXPOSE 8080
ENTRYPOINT [ "java","-jar","/app/app.jar" ]