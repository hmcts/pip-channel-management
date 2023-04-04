package uk.gov.hmcts.reform.pip.channel.management.services;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ChannelNotFoundException;
import uk.gov.hmcts.reform.pip.channel.management.utils.ThirdPartyApi;
import uk.gov.hmcts.reform.pip.model.subscription.Channel;
import uk.gov.hmcts.reform.pip.model.subscription.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriberListServiceTest {

    private static final String COURTEL_VALUE = "testCourtelValue";

    @Mock
    private AccountManagementService accountManagementService;

    @Mock
    private ThirdPartyApi thirdPartyApi;

    @InjectMocks
    SubscriberListService subscriberListService;


    private static final Subscription SUB1 = new Subscription();
    private static final Subscription SUB2 = new Subscription();
    private static final Subscription SUB3 = new Subscription();
    private static final String USER1 = "testUser1";
    private static final String USER2 = "testUser2";
    private static final String TEST_EMAIL_1 = "test@user.com";
    private static final String TEST_EMAIL_2 = "dave@email.com";

    @Test
    void buildEmailSubscriptionMap() {
        Map<String, Optional<String>> userEmailsMap = new ConcurrentHashMap<>();
        userEmailsMap.put(USER2, Optional.of(TEST_EMAIL_2));
        userEmailsMap.put(USER1, Optional.of(TEST_EMAIL_1));

        SUB1.setUserId(USER1);
        SUB2.setUserId(USER2);
        Map<String, List<Subscription>> expectedMap = new ConcurrentHashMap<>();
        expectedMap.put(TEST_EMAIL_1, List.of(SUB1));
        expectedMap.put(TEST_EMAIL_2, List.of(SUB2));
        doReturn(userEmailsMap).when(accountManagementService).getEmails(any());
        List<Subscription> initialList = List.of(SUB1, SUB2);
        assertEquals(subscriberListService.buildEmailSubscriptionMap(initialList), expectedMap,
                     "the final map produced is not equivalent to the expected output");
    }

    @Test
    void testBuildEmailSubscriptionMapThrows() {
        List<Subscription> initialList = List.of(SUB1, SUB2);
        when(accountManagementService.getEmails(any())).thenReturn(new ConcurrentHashMap<>());
        ChannelNotFoundException ex = assertThrows(ChannelNotFoundException.class, () ->
            subscriberListService.buildEmailSubscriptionMap(initialList),
                                                   "Expected exception to be thrown");
        assertEquals("No email channel found for any of the users provided", ex.getMessage(),
                     "Messages should match");
    }

    @Test
    void unduplicatedSubsMap() {
        List<Subscription> subscriptionList = new ArrayList<>();
        SUB1.setUserId(USER1);
        SUB2.setUserId(USER2);
        subscriptionList.add(SUB1);
        subscriptionList.add(SUB2);
        Map<String, List<Subscription>> expectedResponse = new ConcurrentHashMap<>();
        expectedResponse.put(USER1, List.of(SUB1));
        expectedResponse.put(USER2, List.of(SUB2));

        Map<String, List<Subscription>> response =
            subscriberListService.deduplicateSubscriptions(subscriptionList);
        assertEquals(response, expectedResponse, "should return a map of users to subscription lists");
    }

    @Test
    void duplicatedSubsMap() {
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
            subscriberListService.deduplicateSubscriptions(subscriptionList);
        assertEquals(response, expectedResponse, "should return a deduplicated map of users to subscription "
            + "lists");
    }

    @Test
    void emailMapWithEmailFound() {
        Map<String, Optional<String>> emailMap = new ConcurrentHashMap<>();
        emailMap.put(USER1, Optional.of(TEST_EMAIL_1));
        emailMap.put(USER2, Optional.of(TEST_EMAIL_2));

        Map<String, List<Subscription>> subsMap = new ConcurrentHashMap<>();
        subsMap.put(USER1, List.of(SUB1, SUB2));
        subsMap.put(USER2, List.of(SUB3));

        Map<String, List<Subscription>> response =
            subscriberListService.userIdToUserEmailSwitcher(subsMap, emailMap);

        Map<String, List<Subscription>> expectedResponse = new ConcurrentHashMap<>();
        expectedResponse.put(TEST_EMAIL_1, List.of(SUB1, SUB2));
        expectedResponse.put(TEST_EMAIL_2, List.of(SUB3));

        assertEquals(expectedResponse, response, "Maps do not equal.");
    }

    @Test
    void emailMapWithEmailNotFound() {
        Map<String, Optional<String>> emailMap = new ConcurrentHashMap<>();
        emailMap.put(USER1, Optional.of(TEST_EMAIL_1));
        emailMap.put(USER2, Optional.empty());

        Map<String, List<Subscription>> subsMap = new ConcurrentHashMap<>();
        subsMap.put(USER1, List.of(SUB1, SUB2));
        subsMap.put(USER2, List.of(SUB3));

        Map<String, List<Subscription>> response =
            subscriberListService.userIdToUserEmailSwitcher(subsMap, emailMap);

        Map<String, List<Subscription>> expectedResponse = new ConcurrentHashMap<>();
        expectedResponse.put(TEST_EMAIL_1, List.of(SUB1, SUB2));

        assertEquals(expectedResponse, response, "Maps do not equal.");
    }

    @Test
    void testBuildApiSubscriptionsMap() {
        when(thirdPartyApi.getCourtel()).thenReturn(COURTEL_VALUE);
        SUB1.setUserId(USER1);
        SUB1.setChannel(Channel.API_COURTEL);
        SUB2.setUserId(USER1);
        SUB2.setChannel(Channel.API_COURTEL);

        Map<String, List<Subscription>> expected = new ConcurrentHashMap<>();
        expected.put(COURTEL_VALUE, List.of(SUB1, SUB2));

        assertEquals(expected, subscriberListService.buildApiSubscriptionsMap(List.of(SUB1, SUB2)),
                     "Maps should match");
    }

    @Test
    void testBuildApiSubscriptionsMapInvalidApiChannel() {
        SUB1.setUserId(USER1);
        SUB1.setChannel(Channel.API_COURTEL);
        SUB2.setUserId(USER2);
        SUB2.setChannel(Channel.EMAIL);
        List<Subscription> subscriptions = List.of(SUB1, SUB2);

        ChannelNotFoundException ex = assertThrows(ChannelNotFoundException.class, () ->
            subscriberListService.buildApiSubscriptionsMap(subscriptions),
                                                   "Should throw ChannelNotFoundException"
            );

        assertEquals("Invalid channel for API subscriptions: EMAIL", ex.getMessage(),
                     "Messages should match");
    }

}
