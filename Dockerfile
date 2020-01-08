FROM openjdk:8
COPY ./build/libs/* /usr/app/
WORKDIR /usr/app
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "DrKleinAWSIoTMiddleware-1.0-SNAPSHOT.jar"]
