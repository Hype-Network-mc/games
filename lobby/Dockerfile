FROM eclipse-temurin:25-jre-alpine

RUN mkdir /app
WORKDIR /app

COPY build/libs/*-all.jar /app/lobby.jar
COPY run/*.polar /app/
COPY run/*.bbmodel /app/

ENTRYPOINT ["java"]
CMD ["-jar", "/app/lobby.jar"]
