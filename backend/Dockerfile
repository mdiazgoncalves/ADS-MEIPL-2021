FROM maven:3.8.4-jdk-8 AS build

ADD /src /src
ADD pom.xml .
RUN mvn package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true


FROM openjdk:8-jre

RUN mkdir app

COPY --from=build /target/dependency /app/dependency
COPY --from=build /target/classes /app/classes

COPY /owl2vowl.jar /app
COPY /config.properties /app

WORKDIR /app

EXPOSE 8080

ENTRYPOINT java -server -cp classes:dependency/* pt.iul.ista.ads.heroku.Main