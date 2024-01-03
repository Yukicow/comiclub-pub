FROM openjdk:17-jdk
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} comiclub.jar
ENTRYPOINT ["java", "-jar", "comiclub.jar"]