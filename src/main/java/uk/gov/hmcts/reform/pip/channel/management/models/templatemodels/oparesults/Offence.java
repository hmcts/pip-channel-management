package uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.oparesults;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Offence {
    private String offenceTitle;
    private String offenceSection;
    private String decisionDate;
    private String decisionDetail;
    private String bailStatus;
    private String nextHearingDate;
    private String nextHearingLocation;
    private String reportingRestrictions;
}
