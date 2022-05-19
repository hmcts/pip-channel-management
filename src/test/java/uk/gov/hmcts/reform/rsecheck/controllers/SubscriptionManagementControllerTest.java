package uk.gov.hmcts.reform.rsecheck.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.demo.controllers.SubscriptionManagementController;
import uk.gov.hmcts.reform.demo.models.Subscription;
import uk.gov.hmcts.reform.demo.services.AccountManagementService;
import uk.gov.hmcts.reform.demo.services.SubscriptionManagementService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionManagementControllerTest {

    @Mock
    private SubscriptionManagementService subscriptionManagementService;

    @Mock
    private AccountManagementService accountManagementService;

    @InjectMocks
    SubscriptionManagementController subscriptionManagementController;

    private static final String STATUS_CODE_MATCH = "Status code responses should match";
    private static final String TEST_EMAIL_1 = "test@user.com";
    private static final String TEST_EMAIL_2 = "dave@email.com";
    private static final UUID UUID1 = UUID.randomUUID();
    private static final UUID UUID2 = UUID.randomUUID();

    @Test
    void singleItemListOfEmails() {
        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(new Subscription());
        List<String> singleItemList = new ArrayList<>();
        singleItemList.add(UUID1.toString());
        Map<String, Optional<String>> returnedEmailMap = new ConcurrentHashMap<>();
        returnedEmailMap.put(UUID1.toString(), Optional.of(TEST_EMAIL_1));
        Map<String, List<Subscription>> returnedSubMap = new ConcurrentHashMap<>();
        returnedSubMap.put(UUID1.toString(), subscriptionList);
        List<String> keyset = new ArrayList<>(returnedSubMap.keySet());
        Map<String, List<Subscription>> finalMap = new ConcurrentHashMap<>();
        finalMap.put(TEST_EMAIL_1, subscriptionList);
        when(accountManagementService.getEmails(keyset)).thenReturn(returnedEmailMap);
        when(subscriptionManagementService.deduplicateSubscriptions(subscriptionList)).thenReturn(returnedSubMap);
        ResponseEntity<Map<String, List<Subscription>>> response =
            subscriptionManagementController.buildSubscriberList(subscriptionList);

        assertEquals(response.getBody(), finalMap, "Map does not match with output");
        assertEquals(response.getStatusCode(), HttpStatus.ACCEPTED, STATUS_CODE_MATCH);
        assertEquals(response.getBody().get(TEST_EMAIL_1), subscriptionList, "Subs list does not match");
    }

    @Test
    void multiItemList() {
        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(new Subscription());
        List<String> multiItemList = new ArrayList<>();
        multiItemList.add(UUID1.toString());
        multiItemList.add(UUID2.toString());
        Map<String, Optional<String>> returnedEmailMap = new ConcurrentHashMap<>();
        returnedEmailMap.put(UUID1.toString(), Optional.of(TEST_EMAIL_1));
        returnedEmailMap.put(UUID2.toString(), Optional.of(TEST_EMAIL_2));
        Map<String, List<Subscription>> returnedSubMap = new ConcurrentHashMap<>();
        returnedSubMap.put(UUID1.toString(), subscriptionList);
        returnedSubMap.put(UUID2.toString(), subscriptionList);
        List<String> keyset = new ArrayList<>(returnedSubMap.keySet());
        Map<String, List<Subscription>> finalMap = new ConcurrentHashMap<>();
        finalMap.put(TEST_EMAIL_1, subscriptionList);
        finalMap.put(TEST_EMAIL_2, subscriptionList);
        when(accountManagementService.getEmails(keyset)).thenReturn(returnedEmailMap);
        when(subscriptionManagementService.deduplicateSubscriptions(subscriptionList)).thenReturn(returnedSubMap);
        ResponseEntity<Map<String, List<Subscription>>> response =
            subscriptionManagementController.buildSubscriberList(subscriptionList);
        assertEquals(response.getBody(), finalMap, "Map does not match with output");
        assertEquals(response.getStatusCode(), HttpStatus.ACCEPTED, STATUS_CODE_MATCH);
        assertEquals(response.getBody().get(TEST_EMAIL_1), subscriptionList, "Subs list does not match");
    }


    @Test
    void emailDontMatch() {
        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(new Subscription());
        List<String> singleItemList = new ArrayList<>();
        singleItemList.add(UUID1.toString());
        Map<String, Optional<String>> returnedEmailMap = new ConcurrentHashMap<>();
        returnedEmailMap.put(UUID1.toString(), Optional.empty());
        Map<String, List<Subscription>> returnedSubMap = new ConcurrentHashMap<>();
        returnedSubMap.put(UUID1.toString(), subscriptionList);
        List<String> keyset = new ArrayList<>(returnedSubMap.keySet());
        when(accountManagementService.getEmails(keyset)).thenReturn(returnedEmailMap);
        when(subscriptionManagementService.deduplicateSubscriptions(subscriptionList)).thenReturn(returnedSubMap);
        ResponseEntity<Map<String, List<Subscription>>> response =
            subscriptionManagementController.buildSubscriberList(subscriptionList);

        assertTrue(response.getBody().isEmpty(), "Map does not match with output");
        assertEquals(response.getStatusCode(), HttpStatus.ACCEPTED, STATUS_CODE_MATCH);
    }

    @Test
    void noEmailsReturned() {
        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(new Subscription());
        List<String> singleItemList = new ArrayList<>();
        singleItemList.add(UUID1.toString());
        Map<String, Optional<String>> returnedEmailMap = new ConcurrentHashMap<>();
        Map<String, List<Subscription>> returnedSubMap = new ConcurrentHashMap<>();
        returnedSubMap.put(UUID1.toString(), subscriptionList);
        List<String> keyset = new ArrayList<>(returnedSubMap.keySet());
        when(accountManagementService.getEmails(keyset)).thenReturn(returnedEmailMap);
        when(subscriptionManagementService.deduplicateSubscriptions(subscriptionList)).thenReturn(returnedSubMap);
        ResponseEntity<Map<String, List<Subscription>>> response =
            subscriptionManagementController.buildSubscriberList(subscriptionList);
        assertEquals(response.getBody(), returnedSubMap, "Map should be empty");
        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND, STATUS_CODE_MATCH);
    }

}
