package uk.gov.hmcts.reform.demo.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class AccountManagementService {

    @Autowired
    private WebClient webClient;

    @Value("${service-to-service.account-management}")
    private String url;

    public Map<String, Optional<String>> getEmails(List<String> listOfUserIds) {
        try {
            return  webClient.post().uri(new URI(String.format(
                    "%s/account/emails/", url))).body(BodyInserters.fromValue(listOfUserIds))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Optional<String>>>() {
                }).block();


        } catch (WebClientException | URISyntaxException ex) {
            log.error(
                "Account management request failed for this map. Response: {}",
                ex.getMessage()
            );
            return null;
        }
    }
}
