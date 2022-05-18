package uk.gov.hmcts.reform.demo.controllers;


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
import uk.gov.hmcts.reform.demo.models.Subscription;
import uk.gov.hmcts.reform.demo.services.AccountManagementService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@Api(tags = "Account management API")
@RequestMapping("/account")
public class AccountManagementController {

    @Autowired
    AccountManagementService accountManagementService;

    @ApiResponses({
        @ApiResponse(code = 202, message = "Subscriber request has been accepted"),
        @ApiResponse(code = 404, message = "No subscribers exist for this list")
    })
    @ApiOperation("Takes in artefact to build subscriber list.")
    @PostMapping("/emails")
    public ResponseEntity<Map<String, Optional<String>>> buildSubscriberList(@RequestBody List<Subscription> listOfSubscriptions){
        log.info(String.format("Received a list of subscribers of length %s", listOfSubscriptions.size()));
        List<String> userIds = new ArrayList<>();
        listOfSubscriptions.forEach(subscription ->{
            userIds.add(subscription.getUserId());
        });

        Map<String, Optional<String>> mapOfUsersAndEmails = accountManagementService.getEmails(userIds);
        if (mapOfUsersAndEmails == null){
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOfUsersAndEmails);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(mapOfUsersAndEmails);
    }
}
