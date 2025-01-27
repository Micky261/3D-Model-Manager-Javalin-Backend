ARG VERSION=latest
FROM eclipse-temurin:17-alpine

ENV STATS_JAVALIN_PORT=8421

# Copy backend
COPY ./jar/3DMM-all.jar /opt/backend/3DMM-all.jar
COPY ./config/app-config.template.json /config/app-config.json

EXPOSE $STATS_JAVALIN_PORT

# Check version endpoint
HEALTHCHECK CMD curl --fail http://localhost:$STATS_JAVALIN_PORT/api/version || exit 1

CMD ["java", "-jar", "/opt/backend/3DMM-all.jar"]
