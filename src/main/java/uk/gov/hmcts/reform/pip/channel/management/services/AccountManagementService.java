package uk.gov.hmcts.reform.pip.channel.management.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;


/**
 * Connects to the account management service to request emails corresponding with subscription userIds.
 */
@Slf4j
@Component
public class AccountManagementService {

    @Value("${service-to-service.account-management}")
    private String url;

    private final WebClient webClient;

    @Autowired
    public AccountManagementService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Map<String, Optional<String>> getEmails(List<String> listOfUserIds) {
        try {
            return webClient.post().uri(String.format("%s/account/emails", url))
                .body(BodyInserters.fromValue(listOfUserIds))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Optional<String>>>() {
                }).block();

        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Request to Account Management to get account e-mails failed with error: %s",
                              ex.getMessage())
            ));
            return Collections.emptyMap();
        }
    }

    /**
     * Calls Account Management to determine whether a user is allowed to see a set publication.
     * @param userId The UUID of the user to retrieve.
     * @param listType The list type of the publication.
     * @param sensitivity The sensitivity of the publication
     * @return A flag indicating whether the user is authorised.
     */
    public boolean getIsAuthorised(UUID userId, ListType listType, Sensitivity sensitivity) {
        try {
            return webClient.get().uri(String.format(
                    "%s/account/isAuthorised/%s/%s/%s", url, userId, listType, sensitivity))
                .retrieve().bodyToMono(Boolean.class).block();
        } catch (WebClientException ex) {
            log.error(writeLog(
                String.format("Request to Account Management to check user authorisation failed with error: %s",
                              ex.getMessage())
            ));
            return false;
        }
    }
}
