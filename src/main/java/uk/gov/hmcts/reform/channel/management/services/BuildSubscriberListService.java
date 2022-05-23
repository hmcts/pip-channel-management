package uk.gov.hmcts.reform.channel.management.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.channel.management.models.external.subscriptionmanagement.Subscription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Service that handles the creation of a per-user Email to List of Subscriptions map which is created from a list of
 * subscriptions that are eligible to be triggred by a new publication. The reasoning behind this is to capture users
 * who are subscribed from multiple different methods to the same publication and prevent them from being emailed
 * multiple times for the same thing.
 */
@Slf4j
@Component
public class BuildSubscriberListService {

    @Autowired
    AccountManagementService accountManagementService;

    /**
     * Parent method which handles the flow through the service, initially capturing duplicate users, then sending a
     * request to the account management microservice to match user ids to emails, then pruning and logging those
     * with no attached email, then building the final map of individual user emails to relevant subscription objects.
     * The deduplication occurs before sending the request to account management for emails to prevent wasteful API use.
     * @param listOfSubs
     * @return A map of user emails to list of subscriptions
     */
    public Map<String, List<Subscription>> buildEmailSubMap(List<Subscription> listOfSubs) {
        Map<String, List<Subscription>> mappedSubscriptions =
            deduplicateSubscriptions(listOfSubs);

        List<String> userIds = new ArrayList<>(mappedSubscriptions.keySet());

        Map<String, Optional<String>> mapOfUsersAndEmails = accountManagementService.getEmails(userIds);

        if (mapOfUsersAndEmails.isEmpty()) {
            return Collections.emptyMap();
        }
        return finalSubscriptionEmailMapHandler(mappedSubscriptions, mapOfUsersAndEmails);
    }

    /**
     * This method accesses the list of subscriptions passed in, and transforms it into a list of user id strings
     * with associated subscriptions for each.
     * @param listOfSubs
     * @return
     */
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

    /**
     * Logs and removes subscribers associated with empty email records (i.e. those with no matching email in account
     * management) as well as handling the flipping of userId to email as the key for the map.
     * @param userIdMap
     * @param userEmailMap
     * @return Builds a final map of email addresses to subscription objects.
     */
    public Map<String, List<Subscription>> finalSubscriptionEmailMapHandler(Map<String, List<Subscription>> userIdMap,
                                                                            Map<String, Optional<String>> userEmailMap) {
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
