package uk.gov.hmcts.reform.channel.management.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.channel.management.models.external.subscriptionmanagement.Subscription;
import uk.gov.hmcts.reform.channel.management.services.AccountManagementService;
import uk.gov.hmcts.reform.channel.management.services.DuplicationAndEmptyHandlerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelManagementControllerTest {

    @Mock
    private DuplicationAndEmptyHandlerService duplicationAndEmptyHandlerService;

    @Mock
    private AccountManagementService accountManagementService;

    @InjectMocks
    ChannelManagementController channelManagementController;

    private static final String STATUS_CODE_MATCH = "Status code responses should match";
    private static final String TEST_EMAIL_1 = "test@user.com";
    private static final String TEST_EMAIL_2 = "dave@email.com";
    private static final String USER_1 = "user1";

    @Test
    void singleItemListOfEmails() {
        List<Subscription> subscriptionList = new ArrayList<>();
        Map<String, Optional<String>> returnedEmailMap = new ConcurrentHashMap<>();
        returnedEmailMap.put("stringToPreventSadPath", Optional.of("Test"));

        Map<String, List<Subscription>> returnedSubMap = new ConcurrentHashMap<>();
        List<String> keyset = new ArrayList<>(returnedSubMap.keySet());

        Map<String, List<Subscription>> finalMap = new ConcurrentHashMap<>();
        finalMap.put(TEST_EMAIL_1, subscriptionList);

        when(accountManagementService.getEmails(keyset)).thenReturn(returnedEmailMap);
        when(duplicationAndEmptyHandlerService.deduplicateSubscriptions(subscriptionList)).thenReturn(returnedSubMap);
        when(duplicationAndEmptyHandlerService.mapCleaner(returnedSubMap, returnedEmailMap)).thenReturn(finalMap);

        ResponseEntity<Map<String, List<Subscription>>> response =
            channelManagementController.buildSubscriberList(subscriptionList);

        assertEquals(response.getBody(), finalMap, "Map does not match with output");
        assertEquals(response.getStatusCode(), HttpStatus.ACCEPTED, STATUS_CODE_MATCH);
        assertEquals(response.getBody().get(TEST_EMAIL_1), subscriptionList, "Subs list does not match");
    }

    @Test
    void multiItemListOfEmails() {
        List<Subscription> subscriptionList = new ArrayList<>();
        Map<String, Optional<String>> returnedEmailMap = new ConcurrentHashMap<>();
        returnedEmailMap.put("stringToPreventSadPath", Optional.of("test"));
        Map<String, List<Subscription>> returnedSubMap = new ConcurrentHashMap<>();
        List<String> keyset = new ArrayList<>(returnedSubMap.keySet());
        Map<String, List<Subscription>> finalMap = new ConcurrentHashMap<>();
        finalMap.put(TEST_EMAIL_1, subscriptionList);
        finalMap.put(TEST_EMAIL_2, subscriptionList);
        when(accountManagementService.getEmails(keyset)).thenReturn(returnedEmailMap);
        when(duplicationAndEmptyHandlerService.deduplicateSubscriptions(subscriptionList)).thenReturn(returnedSubMap);
        when(duplicationAndEmptyHandlerService.mapCleaner(returnedSubMap, returnedEmailMap)).thenReturn(finalMap);
        ResponseEntity<Map<String, List<Subscription>>> response =
            channelManagementController.buildSubscriberList(subscriptionList);
        assertEquals(response.getBody(), finalMap, "Map does not match with output");
        assertEquals(response.getStatusCode(), HttpStatus.ACCEPTED, STATUS_CODE_MATCH);
        assertEquals(response.getBody().get(TEST_EMAIL_1), subscriptionList, "Subs list does not match");
    }


    @Test
    void emailDontMatch() {
        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(new Subscription());
        Map<String, Optional<String>> returnedEmailMap = new ConcurrentHashMap<>();
        returnedEmailMap.put(USER_1, Optional.empty());
        Map<String, List<Subscription>> returnedSubMap = new ConcurrentHashMap<>();
        returnedSubMap.put(USER_1, subscriptionList);
        List<String> keyset = new ArrayList<>(returnedSubMap.keySet());
        when(accountManagementService.getEmails(keyset)).thenReturn(returnedEmailMap);
        when(duplicationAndEmptyHandlerService.deduplicateSubscriptions(subscriptionList)).thenReturn(returnedSubMap);
        ResponseEntity<Map<String, List<Subscription>>> response =
            channelManagementController.buildSubscriberList(subscriptionList);

        assertTrue(response.getBody().isEmpty(), "Map does not match with output");
        assertEquals(response.getStatusCode(), HttpStatus.ACCEPTED, STATUS_CODE_MATCH);
    }

    @Test
    void noEmailsReturned() {
        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(new Subscription());
        Map<String, Optional<String>> returnedEmailMap = new ConcurrentHashMap<>();
        Map<String, List<Subscription>> returnedSubMap = new ConcurrentHashMap<>();
        returnedSubMap.put(USER_1, subscriptionList);
        List<String> keyset = new ArrayList<>(returnedSubMap.keySet());
        when(accountManagementService.getEmails(keyset)).thenReturn(returnedEmailMap);
        when(duplicationAndEmptyHandlerService.deduplicateSubscriptions(subscriptionList)).thenReturn(returnedSubMap);
        ResponseEntity<Map<String, List<Subscription>>> response =
            channelManagementController.buildSubscriberList(subscriptionList);
        assertEquals(response.getBody(), returnedSubMap, "Map should be empty");
        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND, STATUS_CODE_MATCH);
    }

}
