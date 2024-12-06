FROM amazoncorretto:21-alpine
COPY build/libs/PopularVote-0.0.1-SNAPSHOT.jar PopularVote.jar

ENV JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=75.0 -XX:MaxRAMPercentage=75.0 -XX:MinRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/PopularVote.jar"]