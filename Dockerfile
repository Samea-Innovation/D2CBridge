FROM openjdk:bullseye AS device

VOLUME /tmp

WORKDIR /opt/Nestwave
COPY target/device-0.0.1-SNAPSHOT.jar appDevice.jar
COPY target/security/ security/
COPY start.sh .

ENTRYPOINT [ "./start.sh" ]
