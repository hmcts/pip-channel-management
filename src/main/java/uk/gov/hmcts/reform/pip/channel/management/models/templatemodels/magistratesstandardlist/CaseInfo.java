package uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.magistratesstandardlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaseInfo {
    private String prosecutionAuthorityCode;
    private String hearingNumber;
    private String attendanceMethod;
    private String caseNumber;
    private String caseSequenceIndicator;
    private String asn;
    private String hearingType;
    private String panel;
    private String convictionDate;
    private String adjournedDate;
}
