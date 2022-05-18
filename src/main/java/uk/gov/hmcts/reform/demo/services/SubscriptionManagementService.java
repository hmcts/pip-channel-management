package uk.gov.hmcts.reform.demo.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.hmcts.reform.demo.models.Subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SubscriptionManagementService {

    public Map<String, List<Subscription>> deduplicateSubscriptions(List<Subscription> listOfSubs) {
        Map<String, List<Subscription>> mapOfSubscriptions = new HashMap<>();
        listOfSubs.forEach(subscription -> {
            List<Subscription> currentList = new ArrayList<>();
            if (mapOfSubscriptions.get(subscription.getUserId()) != null) {
                    currentList = mapOfSubscriptions.get(subscription.getUserId());
            }
            currentList.add(subscription);
            mapOfSubscriptions.put(subscription.getUserId(), currentList);
        });
        return mapOfSubscriptions;
    }


}
