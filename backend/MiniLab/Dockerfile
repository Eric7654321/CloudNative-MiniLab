FROM maven:3.8.5-openjdk-17-slim AS build

COPY . /build
RUN cd /build \
    && mvn clean package -DskipTests

FROM openjdk:21

ARG HOST
ARG PASS
ARG PORT
ARG USER

ENV DB_HOST=$HOST
ENV DB_PASS=$PASS
ENV DB_PORT=$PORT
ENV DB_USER=$USER

COPY --from=build /build/target/MiniLab-1.0.1-SNAPSHOT.jar /App/MiniLab-1.0.1-SNAPSHOT.jar
WORKDIR /App

CMD [ "java", "-jar", "MiniLab-1.0.1-SNAPSHOT.jar" ]
