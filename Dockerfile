# First stage: Build the WAR file
FROM maven:3.6.3-jdk-8 AS build
WORKDIR /app

COPY ./pom.xml ./
#COPY ./.mvn ./.mvn

RUN mvn dependency:go-offline -B

COPY ./src ./src

# Create logs directory in the container
#RUN mkdir -p /app/logs

# Set proper permissions if necessary
#RUN chmod 755 /app/logs

# Run Maven to build the project and create the WAR file
RUN mvn clean package -DskipTests

# Second stage: Create the final image with the WAR file
FROM openjdk:8-jdk
WORKDIR /app

# Copy the WAR file from the first stage
COPY --from=build /app/target/test_tr069-0.0.1-SNAPSHOT.war /app/test_tr069-0.0.1-SNAPSHOT.war

# Expose the application port
EXPOSE 7547

# Run the WAR file
ENTRYPOINT ["java", "-jar", "/app/test_tr069-0.0.1-SNAPSHOT.war"]
