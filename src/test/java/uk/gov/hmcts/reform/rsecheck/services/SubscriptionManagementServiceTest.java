package uk.gov.hmcts.reform.rsecheck.services;


import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.demo.models.Subscription;
import uk.gov.hmcts.reform.demo.services.SubscriptionManagementService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubscriptionManagementServiceTest {

    SubscriptionManagementService subscriptionManagementService = new SubscriptionManagementService();

    private static final Subscription SUB1 = new Subscription();
    private static final Subscription SUB2 = new Subscription();
    private static final Subscription SUB3 = new Subscription();
    private static final String USER1 = "testUser1";
    private static final String USER2 = "testUser2";

    @Test
    void unduplicatedMap() {
        List<Subscription> subscriptionList = new ArrayList<>();
        SUB1.setUserId(USER1);
        SUB2.setUserId(USER2);
        subscriptionList.add(SUB1);
        subscriptionList.add(SUB2);
        Map<String, List<Subscription>> expectedResponse = new ConcurrentHashMap<>();
        expectedResponse.put(USER1, List.of(SUB1));
        expectedResponse.put(USER2, List.of(SUB2));

        Map<String, List<Subscription>> response =
            subscriptionManagementService.deduplicateSubscriptions(subscriptionList);
        assertEquals(response, expectedResponse, "should return a map of users to subscription lists");

    }

    @Test
    void duplicatedMap() {
        SUB1.setUserId(USER1);
        SUB2.setUserId(USER1);
        SUB3.setUserId(USER2);
        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(SUB1);
        subscriptionList.add(SUB2);
        subscriptionList.add(SUB3);
        Map<String, List<Subscription>> expectedResponse = new ConcurrentHashMap<>();
        expectedResponse.put(USER1, List.of(SUB1, SUB2));
        expectedResponse.put(USER2, List.of(SUB3));

        Map<String, List<Subscription>> response =
            subscriptionManagementService.deduplicateSubscriptions(subscriptionList);
        assertEquals(response, expectedResponse, "should return a deduplicated map of users to subscription "
            + "lists");
    }

}
