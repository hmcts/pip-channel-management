package uk.gov.hmcts.reform.pip.channel.management.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ChannelNotFoundException;
import uk.gov.hmcts.reform.pip.channel.management.utils.ThirdPartyApi;
import uk.gov.hmcts.reform.pip.model.subscription.Channel;
import uk.gov.hmcts.reform.pip.model.subscription.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;


/**
 * Service that handles the creation of a per-user Email to List of Subscriptions map which is created from a list of
 * subscriptions that are eligible to be triggred by a new publication. The reasoning behind this is to capture users
 * who are subscribed from multiple different methods to the same publication and prevent them from being emailed
 * multiple times for the same thing.
 */
@Slf4j
@Component
public class SubscriberListService {

    private final AccountManagementService accountManagementService;
    private final ThirdPartyApi thirdPartyApi;

    @Autowired
    public SubscriberListService(AccountManagementService accountManagementService, ThirdPartyApi thirdPartyApi) {
        this.accountManagementService = accountManagementService;
        this.thirdPartyApi = thirdPartyApi;
    }

    /**
     * Parent method which handles the flow through the service, initially capturing duplicate users, then sending a
     * request to the account management microservice to match user ids to emails, then pruning and logging those
     * with no attached email, then building the final map of individual user emails to relevant subscription objects.
     * The deduplication occurs before sending the request to account management for emails to prevent wasteful API use.
     * @param listOfSubs - a list of subscription objects associated with a publication
     * @return A map of user emails to list of subscriptions
     */
    public Map<String, List<Subscription>> buildEmailSubscriptionMap(List<Subscription> listOfSubs) {
        Map<String, List<Subscription>> mappedSubscriptions =
            deduplicateSubscriptions(listOfSubs);

        List<String> userIds = new ArrayList<>(mappedSubscriptions.keySet());

        Map<String, Optional<String>> mapOfUsersAndEmails = accountManagementService.getEmails(userIds);

        if (mapOfUsersAndEmails.values().stream().allMatch(Optional::isEmpty)) {
            throw new ChannelNotFoundException("No email channel found for any of the users provided");
        }
        return userIdToUserEmailSwitcher(mappedSubscriptions, mapOfUsersAndEmails);
    }

    /**
     * Creates map of third party api urls and a list of Subscriptions associated with them.
     * @param subscriptions list of subscriptions to be trimmed of duplications and associated with an api.
     * @return Map of Url to list of subscriptions.
     */
    public Map<String, List<Subscription>> buildApiSubscriptionsMap(List<Subscription> subscriptions) {
        return userIdToApiValueSwitcher(deduplicateSubscriptions(subscriptions));
    }

    /**
     * This method accesses the list of subscriptions passed in, and transforms it into a list of user id strings
     * with associated subscriptions for each.
     * @param listOfSubs - a list of subscriptions for a given object.
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
     * management) as well as handling the flipping of userId to email as the key for the map
     * @param userIdMap - A map of userIds to the list of subscription objects associated with them.
     * @param userEmailMap - a map of userIds to their email addresses (optional in case they don't exist in account
     *                     management.)
     * @return Map of email addresses to subscription objects.
     */
    public Map<String, List<Subscription>> userIdToUserEmailSwitcher(Map<String, List<Subscription>> userIdMap,
                                                                     Map<String, Optional<String>> userEmailMap) {
        Map<String, List<Subscription>> cloneMap = new ConcurrentHashMap<>(userIdMap);

        cloneMap.forEach((userId, subscriptions) -> {

            if (userEmailMap.get(userId).isEmpty()) {
                log.error(writeLog(String.format("No email with user ID %s found", userId)));
            } else {
                userIdMap.put(userEmailMap.get(userId).get(), subscriptions);
            }
            userIdMap.remove(userId);
        });
        return userIdMap;
    }

    /**
     * Takes in Map and replaces the user id thats not needed for third party, to the URL the subscription needs to
     * be sent to.
     * @param subscriptions Map of user id's to list of subscriptions.
     * @return Map of URL's to list of subscriptions.
     */
    private Map<String, List<Subscription>> userIdToApiValueSwitcher(Map<String, List<Subscription>> subscriptions) {
        Map<String, List<Subscription>> switchedMap = new ConcurrentHashMap<>();
        subscriptions.forEach((recipient, subscriptionList)  -> {
            if (subscriptionList.get(0).getChannel() == Channel.API_COURTEL) {
                switchedMap.put(thirdPartyApi.getCourtel(), subscriptionList);
            } else {
                throw new ChannelNotFoundException("Invalid channel for API subscriptions: "
                                                       + subscriptionList.get(0).getChannel());
            }
        });
        return switchedMap;
    }
}
