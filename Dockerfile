FROM openliberty/open-liberty:kernel-java11-openj9-ubi

# Copy the open liberty config and .war
COPY --chown=1001:0 src/main/liberty/config/ /config/
COPY --chown=1001:0 src/main/resources/META-INF/ /META-INF/
COPY --chown=1001:0 src/main/liberty/config/server.env.prod /config/server.env
COPY --chown=1001:0 target/*.war /config/apps/