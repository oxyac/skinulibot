FROM gradle:jdk22 as build

WORKDIR /build

RUN mkdir -p /root/.ssh \
    && chmod 0700 /root/.ssh \
    && ssh-keyscan -t rsa github.com >> /root/.ssh/known_hosts \
    && git clone https://github.com/oxyac/skinulibot.git . \
    && gradle build

FROM bellsoft/liberica-openjre-debian:22
COPY --from=build /build/build/libs/skinulibot-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
