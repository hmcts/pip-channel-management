package uk.gov.hmcts.reform.rsecheck.services;


import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.demo.models.Subscription;
import uk.gov.hmcts.reform.demo.services.SubscriptionManagementService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubscriptionManagementServiceTest {

    SubscriptionManagementService subscriptionManagementService = new SubscriptionManagementService();

    private static final Map<String, List<Subscription>> inputMap = new HashMap<>();
    private static final Subscription sub1 = new Subscription();
    private static final Subscription sub2 = new Subscription();
    private static final Subscription sub3 = new Subscription();
    private static final String user1 = "testUser1";
    private static final String user2 = "testUser2";

    @Test
    void unduplicatedMap() {
        List<Subscription> subscriptionList = new ArrayList<>();
        sub1.setUserId(user1);
        sub2.setUserId(user2);
        subscriptionList.add(sub1);
        subscriptionList.add(sub2);
        Map<String, List<Subscription>> expectedResponse = new HashMap<>();
        expectedResponse.put(user1, List.of(sub1));
        expectedResponse.put(user2, List.of(sub2));

        Map<String, List<Subscription>> response =
            subscriptionManagementService.deduplicateSubscriptions(subscriptionList);
        assertEquals(response, expectedResponse);

    }

    @Test
    void duplicatedMap() {
        List<Subscription> subscriptionList = new ArrayList<>();
        sub1.setUserId(user1);
        sub2.setUserId(user1);
        sub3.setUserId(user2);
        subscriptionList.add(sub1);
        subscriptionList.add(sub2);
        subscriptionList.add(sub3);
        Map<String, List<Subscription>> expectedResponse = new HashMap<>();
        expectedResponse.put(user1, List.of(sub1, sub2));
        expectedResponse.put(user2, List.of(sub3));

        Map<String, List<Subscription>> response =
            subscriptionManagementService.deduplicateSubscriptions(subscriptionList);
        assertEquals(response, expectedResponse);
    }

}
