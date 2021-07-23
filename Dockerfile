FROM maven:3-openjdk-11

WORKDIR /opt

COPY target/app.jar .
COPY target/app.jar.original app-original.jar

CMD ["java", "-jar", "app.jar"]
