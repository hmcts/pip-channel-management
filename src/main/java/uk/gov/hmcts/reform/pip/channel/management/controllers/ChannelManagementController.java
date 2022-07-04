package uk.gov.hmcts.reform.pip.channel.management.controllers;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.channel.management.authentication.roles.IsAdmin;
import uk.gov.hmcts.reform.pip.channel.management.models.external.subscriptionmanagement.Subscription;
import uk.gov.hmcts.reform.pip.channel.management.services.AccountManagementService;
import uk.gov.hmcts.reform.pip.channel.management.services.SubscriberListService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.pip.model.LogBuilder.writeLog;

@Slf4j
@RestController
@Api(tags = "Channel management API - takes in triggered subscription objects and then performs relevant tasks to "
    + "ensure they are handled correctly whether they are email-based or API-based subscribers (determined by their "
    + "Channel attribute")
@RequestMapping("/channel")
@IsAdmin
public class ChannelManagementController {

    @Autowired
    AccountManagementService accountManagementService;

    @Autowired
    SubscriberListService subscriberListService;

    @ApiResponses({
        @ApiResponse(code = 200, message = "Subscriber request has been accepted"),
        @ApiResponse(code = 404, message = "No subscribers exist for this list")
    })
    @ApiOperation("Takes in list of subscriptions to build subscriber list.")
    @PostMapping("/emails")
    public ResponseEntity<Map<String, List<Subscription>>> buildSubscriberList(
        @RequestBody List<Subscription> listOfSubscriptions) {
        log.info(writeLog(String.format("Received a list of subscribers of length %s",
                                               listOfSubscriptions.size())));

        Map<String, List<Subscription>> returnMap =
            subscriberListService.buildEmailSubscriptionMap(listOfSubscriptions);

        return ResponseEntity.ok(returnMap);

    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "{Map of api destination to subscriptions}"),
        @ApiResponse(code = 404, message = "Invalid channel for API subscriptions: {channel}")
    })
    @ApiOperation("Returns Map of api destination against list of subscriptions")
    @PostMapping("/api")
    public ResponseEntity<Map<String, List<Subscription>>> returnThirdPartyApi(
        @RequestBody List<Subscription> subscriptions) {
        return ResponseEntity.ok(subscriberListService.buildApiSubscriptionsMap(subscriptions));
    }
}
