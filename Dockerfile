# Stage 1
FROM ghcr.io/graalvm/native-image-community:21-muslib AS builder

WORKDIR /build

COPY . /build

RUN ./mvnw clean install -DskipTests \
    && cd webserver/ \
    && ./mvnw --no-transfer-progress -Pnative native:compile -DskipTests

# Stage 2
FROM gcr.io/distroless/static-debian12

COPY --from=builder /build/webserver/target/webserver ./

ENTRYPOINT ["./webserver"]

## stage 1: build stage
#FROM maven:3.9.6-amazoncorretto-21-al2023 as builder
#
## working directory
#WORKDIR /build
#
## copy source code into build directory
#COPY . /build
#
## generate jar file for webserver directory
#RUN mvn clean --no-transfer-progress install -DskipTests
#
## stage 2: run stage
#FROM maven:3.9.6-amazoncorretto-21-al2023
#
## set working directory
#WORKDIR /app
#
## copy jar file into app directory
#COPY --from=builder /build/webserver/target/webserver-exec.jar /app
#
## use entry point instead of command as it cannot be override
#ENTRYPOINT ["java", "-jar", "/app/webserver-exec.jar"]