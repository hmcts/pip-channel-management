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
import uk.gov.hmcts.reform.pip.channel.management.services.BuildSubscriberListService;

import java.util.List;
import java.util.Map;

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
    BuildSubscriberListService buildSubscriberListService;

    @ApiResponses({
        @ApiResponse(code = 202, message = "Subscriber request has been accepted"),
        @ApiResponse(code = 404, message = "No subscribers exist for this list")
    })
    @ApiOperation("Takes in artefact to build subscriber list.")
    @PostMapping("/emails")
    public ResponseEntity<Map<String, List<Subscription>>> buildSubscriberList(
        @RequestBody List<Subscription> listOfSubscriptions) {
        log.info(String.format("Received a list of subscribers of length %s", listOfSubscriptions.size()));

        Map<String, List<Subscription>> returnMap =
            buildSubscriberListService.buildEmailSubscriptionMap(listOfSubscriptions);

        return ResponseEntity.ok(returnMap);

    }
}
