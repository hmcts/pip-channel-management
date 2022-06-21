package uk.gov.hmcts.reform.pip.channel.management.models.external.subscriptionmanagement;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Subscription {
    private String userId;
    private Channel channel;
    private LocalDateTime createdDate;
    private String caseNumber;
    private String urn;
    private String locationName;
    private String searchValue;
    private SearchType searchType;
}
