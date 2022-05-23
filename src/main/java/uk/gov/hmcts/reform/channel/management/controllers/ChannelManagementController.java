package uk.gov.hmcts.reform.channel.management.controllers;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.channel.management.models.external.subscriptionmanagement.Subscription;
import uk.gov.hmcts.reform.channel.management.services.AccountManagementService;
import uk.gov.hmcts.reform.channel.management.services.BuildSubscriberListService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Api(tags = "Channel management API - middle endpoint for subscription-management -> account-management requests.")
@RequestMapping("/account")
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

        Map<String, List<Subscription>> returnMap = buildSubscriberListService.buildEmailSubMap(listOfSubscriptions);

        if (returnMap.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(returnMap);
        } else {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(returnMap);
        }
    }
}
