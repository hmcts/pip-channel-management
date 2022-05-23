package uk.gov.hmcts.reform.channel.management.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.channel.management.models.external.subscriptionmanagement.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DuplicationAndEmptyHandlerService {

    public Map<String, List<Subscription>> deduplicateSubscriptions(List<Subscription> listOfSubs) {
        Map<String, List<Subscription>> mapOfSubscriptions = new ConcurrentHashMap<>();
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

    public Map<String, List<Subscription>> mapCleaner(Map<String, List<Subscription>> userIdMap, Map<String,
        Optional<String>> userEmailMap) {

        Map<String, List<Subscription>> cloneMap = new ConcurrentHashMap<>(userIdMap);

        cloneMap.forEach((userId, subscriptions) -> {

            if (userEmailMap.get(userId).isEmpty()) {
                log.info(userId + "- no email found.");
            } else {
                userIdMap.put(userEmailMap.get(userId).get(), subscriptions);
            }
            userIdMap.remove(userId);
        });

        log.info(userIdMap.toString());
        return userIdMap;
    }

}
