# Build Java application
FROM docker.io/library/maven:3.8.5-openjdk-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Final image
FROM nginx:alpine
RUN apk add --no-cache openjdk17 supervisor

# Copy JAR file of the application
COPY --from=build /build/target/TelegramWebApp-1.0.0-RELEASE.jar /app/TelegramWebApp.jar

# Copy HTML files
COPY --from=build /build/src/main/resources/templates/html/ /usr/share/nginx/html/

# Copy CSS and JS files
COPY --from=build /build/src/main/resources/templates/css/ /usr/share/nginx/html/css/
COPY --from=build /build/src/main/resources/templates/js/ /usr/share/nginx/html/js/

# Copy images
COPY --from=build /build/src/main/resources/static/images/product_icons/ /data/product_icons/

# Copy database into container
COPY src/main/resources/database.sqlite /data/database.sqlite

# Copy configuration files
COPY nginx.conf /etc/nginx/nginx.conf
COPY supervisord.ini /etc/supervisord.ini

# Copy application.properties
COPY src/main/resources/application.properties /app/application.properties

# Set permissions
RUN chmod -R 755 /usr/share/nginx/html
RUN mkdir -p /data/product_icons && chmod -R 755 /data

# Expose ports
EXPOSE 8080 8081

# Start supervisord
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.ini"]