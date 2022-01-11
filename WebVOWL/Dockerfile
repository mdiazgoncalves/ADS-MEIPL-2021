###########
# WebVOWL #
###########

# Use tomcat java 8 alpine as base image
FROM tomcat:9-jre8-alpine

# Build time arguments (WebVOWL version)
ARG version=1.1.7

COPY .env_docker .env

# Download WebVOWL to tomcat webapps directory as root app
RUN rm -rf /usr/local/tomcat/webapps/*
    
COPY webvowl_${version}.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
# Run default server
CMD ["catalina.sh", "run"]