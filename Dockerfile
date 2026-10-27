# Build the existing Maven WAR application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Compile, run unit tests and package the WAR
RUN mvn -B clean verify


# Run the WAR on Tomcat 9
FROM tomcat:9.0-jdk17-temurin-noble

ENV PORT=10000

# Remove Tomcat's default sample applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy the application at the website root
COPY --from=build \
    /app/target/Daily_Budget_Planner.war \
    /usr/local/tomcat/webapps/ROOT.war

COPY start-tomcat.sh /usr/local/bin/start-tomcat.sh

RUN chmod +x /usr/local/bin/start-tomcat.sh

EXPOSE 10000

ENTRYPOINT ["/usr/local/bin/start-tomcat.sh"]