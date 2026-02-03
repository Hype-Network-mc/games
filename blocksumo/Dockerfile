FROM eclipse-temurin:25-jre-alpine

RUN mkdir /app
WORKDIR /app

COPY build/libs/*-all.jar /app/blocksumo.jar
COPY run/maps /app/maps

ENTRYPOINT ["java"]
CMD ["-jar", "/app/blocksumo.jar"]
