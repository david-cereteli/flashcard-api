# syntax=docker/dockerfile:1

# Build source with maven
FROM maven:3.6.3-jdk-11-slim@sha256:68ce1cd457891f48d1e137c7d6a4493f60843e84c9e2634e3df1d3d5b381d36c AS build
RUN mkdir /project
COPY . /project
WORKDIR /project
RUN mvn clean package -DskipTests

# Select EXACT jre to use with SHA
FROM openjdk:11-jre-slim@sha256:cc785a6f253221e4aebc5bbec47c5560cd6436ae09a0d900275d74af80462b0e
# Create directory in image
RUN mkdir /app
# Create a group and user with limited authorization
RUN addgroup --gid 1001 --system javauser && adduser --system --shell /bin/false --gid 1001 juser
# Copy jar to image
COPY --from=build /project/target/flashcard-1.0.0.jar /app/flashcard.jar
# Select working directory in docker image (default dir)
WORKDIR /app
# Change ownership of files (recursively)
RUN chown -R juser:javauser /app
# All future commands should run as the javauser user
USER juser
# Expose the in-container-port (documentation only)
EXPOSE 8080
# Execute jar in image
CMD "java" "-jar" "flashcard.jar"
