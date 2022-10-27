package uk.gov.hmcts.reform.pip.channel.management.models.templatemodels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TribunalNationalList {
    private String hearingDate;
    private String caseName;
    private String duration;
    private String hearingType;
    private String venue;
}
