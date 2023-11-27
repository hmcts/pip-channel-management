package uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.opapresslist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpaCaseInfo {
    private String urn = "";
    private String scheduledHearingDate = "";
    private String caseReportingRestriction = "";
}
