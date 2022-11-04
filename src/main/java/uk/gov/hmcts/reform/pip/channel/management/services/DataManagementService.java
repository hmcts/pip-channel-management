package uk.gov.hmcts.reform.pip.channel.management.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ServiceToServiceException;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Artefact;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Location;

import java.util.UUID;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Service
@SuppressWarnings({"PMD.PreserveStackTrace", "PMD.ImmutableField", "PMD.LawOfDemeter"})
public class DataManagementService {

    private static final String SERVICE = "Data Management";
    private static final String ADMIN_HEADER = "x-admin";
    private static final String TRUE = "true";

    @Value("${service-to-service.data-management}")
    private String url;

    private WebClient webClient;

    @Autowired
    public DataManagementService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Artefact getArtefact(UUID artefactId) {
        try {
            return webClient.get().uri(String.format("%s/publication/%s", url, artefactId))
                .header(ADMIN_HEADER, TRUE)
                .attributes(clientRegistrationId("dataManagementApi"))
                .retrieve()
                .bodyToMono(Artefact.class).block();
        } catch (WebClientResponseException ex) {
            if (HttpStatus.NOT_FOUND.equals(ex.getStatusCode())) {
                throw new NotFoundException(String.format("Artefact with id %s not found", artefactId));
            } else {
                throw new ServiceToServiceException(SERVICE, ex.getMessage());
            }
        }
    }

    public Location getLocation(String locationId) {
        try {
            return webClient.get().uri(String.format("%s/locations/%s", url, locationId))
                .attributes(clientRegistrationId("dataManagementApi"))
                .retrieve()
                .bodyToMono(Location.class).block();
        } catch (WebClientResponseException ex) {
            if (HttpStatus.NOT_FOUND.equals(ex.getStatusCode())) {
                throw new NotFoundException(String.format("Location with id %s not found", locationId));
            } else {
                throw new ServiceToServiceException(SERVICE, ex.getMessage());
            }
        }
    }

    public String getArtefactJsonBlob(UUID artefactId) {
        try {
            return webClient.get().uri(String.format("%s/publication/%s/payload", url, artefactId))
                .header(ADMIN_HEADER, TRUE)
                .attributes(clientRegistrationId("dataManagementApi"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(String.class).block();
        } catch (WebClientResponseException ex) {
            if (HttpStatus.NOT_FOUND.equals(ex.getStatusCode())) {
                throw new NotFoundException(String.format("Artefact with id %s not found", artefactId));
            } else {
                throw new ServiceToServiceException(SERVICE, ex.getMessage());
            }
        }
    }
}
