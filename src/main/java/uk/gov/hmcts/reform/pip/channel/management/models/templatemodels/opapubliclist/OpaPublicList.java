package uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.opapubliclist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.opapresslist.OpaCaseInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpaPublicList {
    private OpaCaseInfo caseInfo;
    private Defendant defendant;
}
