ARG APP_INSIGHTS_AGENT_VERSION=3.4.14
FROM hmctspublic.azurecr.io/base/java:17-distroless

ENV APP pip-channel-management.jar

COPY lib/applicationinsights.json /opt/app/
COPY lib/openSans.ttf /opt/app/
COPY build/libs/$APP /opt/app/

EXPOSE 8181
CMD [ "pip-channel-management.jar" ]
