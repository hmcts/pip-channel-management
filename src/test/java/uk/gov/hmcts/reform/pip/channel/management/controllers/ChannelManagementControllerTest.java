package uk.gov.hmcts.reform.pip.channel.management.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.channel.management.services.SubscriberListService;
import uk.gov.hmcts.reform.pip.model.subscription.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelManagementControllerTest {

    private final List<Subscription> subscriptionList = new ArrayList<>();
    private final  Map<String, List<Subscription>> returnedMap = new ConcurrentHashMap<>();

    @Mock
    private SubscriberListService subscriberListService;

    @InjectMocks
    ChannelManagementController channelManagementController;

    private static final String STATUS_CODE_MATCH = "Status code responses should match";
    private static final String TEST_EMAIL_1 = "test@user.com";
    private static final String TEST_EMAIL_2 = "dave@email.com";

    @BeforeEach
    void setup() {
        returnedMap.put(TEST_EMAIL_1, subscriptionList);
        returnedMap.put(TEST_EMAIL_2, subscriptionList);
    }

    @Test
    void multiItemListOfEmails() {
        when(subscriberListService.buildEmailSubscriptionMap(subscriptionList)).thenReturn(returnedMap);
        ResponseEntity<Map<String, List<Subscription>>> response =
            channelManagementController.buildSubscriberList(subscriptionList);
        assertEquals(response.getBody(), returnedMap, "Map does not match with output");
        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(response.getBody().get(TEST_EMAIL_1), subscriptionList, "Subs list does not match");
    }

    @Test
    void testReturnThirdPartyApiReturnsOk() {
        when(subscriberListService.buildApiSubscriptionsMap(subscriptionList)).thenReturn(returnedMap);
        ResponseEntity<Map<String, List<Subscription>>> response =
            channelManagementController.returnThirdPartyApi(subscriptionList);
        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_CODE_MATCH);
    }

    @Test
    void testReturnThirdPartyApiReturns() {
        when(subscriberListService.buildApiSubscriptionsMap(subscriptionList)).thenReturn(returnedMap);
        ResponseEntity<Map<String, List<Subscription>>> response =
            channelManagementController.returnThirdPartyApi(subscriptionList);
        assertEquals(returnedMap, response.getBody(), "Returned map should match");
    }

}
