# stage 1
FROM ghcr.io/graalvm/native-image-community:21-muslib AS builder

RUN microdnf -y module enable nodejs:20 && microdnf -y install nodejs && microdnf clean all && rm -rf /var/cache/yum

WORKDIR /build

COPY . /build

RUN ./mvnw clean install -DskipTests \
    && cd webserver/ \
    && ./mvnw --no-transfer-progress -Pnative native:compile -DskipTests

# stage 2
FROM gcr.io/distroless/static-debian12

COPY --from=builder /build/webserver/target/webserver ./

ENTRYPOINT ["./webserver"]