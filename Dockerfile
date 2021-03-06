FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.2
ARG APP_INSIGHTS_AGENT_VERSION=2.5.1
ARG APP
# Application image


COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/$APP /opt/app/

EXPOSE 8181
CMD [ "pip-channel-management.jar" ]
