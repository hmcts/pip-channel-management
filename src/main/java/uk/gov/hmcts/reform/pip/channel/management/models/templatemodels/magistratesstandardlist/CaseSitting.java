package uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.magistratesstandardlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaseSitting {
    private String sittingStartTime;
    private String sittingDuration;
    private CaseInfo caseInfo;
    private List<Offence> offences;
}
