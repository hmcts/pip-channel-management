package uk.gov.hmcts.reform.pip.channel.management.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.channel.management.services.SubscriberListService;
import uk.gov.hmcts.reform.pip.model.authentication.roles.IsAdmin;
import uk.gov.hmcts.reform.pip.model.subscription.Subscription;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@RestController
@Tag(name = "Channel management API - takes in triggered subscription objects and then performs relevant tasks to "
    + "ensure they are handled correctly whether they are email-based or API-based subscribers (determined by their "
    + "Channel attribute")
@RequestMapping("/channel")
@IsAdmin
public class ChannelManagementController {

    private final SubscriberListService subscriberListService;

    @Autowired
    public ChannelManagementController(SubscriberListService subscriberListService) {
        this.subscriberListService = subscriberListService;
    }

    @ApiResponse(responseCode = "200", description = "Subscriber request has been accepted")
    @ApiResponse(responseCode = "404", description = "No subscribers exist for this list")
    @Operation(description = "Takes in list of subscriptions to build subscriber list.")
    @PostMapping("/emails")
    public ResponseEntity<Map<String, List<Subscription>>> buildSubscriberList(
        @RequestBody List<Subscription> listOfSubscriptions) {
        log.info(writeLog(String.format("Received a list of subscribers of length %s",
                                               listOfSubscriptions.size())));

        Map<String, List<Subscription>> returnMap =
            subscriberListService.buildEmailSubscriptionMap(listOfSubscriptions);

        return ResponseEntity.ok(returnMap);
    }

    @ApiResponse(responseCode = "200", description = "{Map of api destination to subscriptions}")
    @ApiResponse(responseCode = "404", description = "Invalid channel for API subscriptions: {channel}")
    @Operation(description = "Returns Map of api destination against list of subscriptions")
    @PostMapping("/api")
    public ResponseEntity<Map<String, List<Subscription>>> returnThirdPartyApi(
        @RequestBody List<Subscription> subscriptions) {
        return ResponseEntity.ok(subscriberListService.buildApiSubscriptionsMap(subscriptions));
    }
}
