FROM eclipse-temurin:19-jre-alpine
EXPOSE 8080
RUN mkdir target
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} target/app.jar
#ARG GITHUB_BRANCH_NAME
#ENV SPRING_PROFILES_ACTIVE=${GITHUB_BRANCH_NAME}
#-Dspring.profiles.active=$SPRING_PROFILES_ACTIVE
ENTRYPOINT exec java -jar target/app.jar