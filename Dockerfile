FROM eclipse-temurin:17

WORKDIR /var/app

COPY public/favicon.png /var/app/public/favicon.png
COPY public/images /var/app/public/images
COPY backend/resources /var/app/resources
COPY backend/goodplace.jar /var/app/goodplace.jar
COPY frontend/target/release/ /var/app/public/js/
COPY frontend/target/webpack/ /var/app/public/js/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "goodplace.jar"]
