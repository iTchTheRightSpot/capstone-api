# Stage 1
FROM ghcr.io/graalvm/native-image-community:21-muslib AS builder

WORKDIR /build

COPY . /build

RUN ./mvnw clean install -DskipTests \
    && cd webserver/ \
    && ./mvnw --no-transfer-progress -Pnative native:compile -DskipTests

# Stage 2
#FROM gcr.io/distroless/static-debian12
FROM alpine:3.20.1

COPY --from=builder /build/webserver/target/webserver ./

ENTRYPOINT ["./webserver"]
