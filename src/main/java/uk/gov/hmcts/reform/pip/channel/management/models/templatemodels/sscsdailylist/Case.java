package uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist;

import lombok.Data;

@Data
public class Case {
    String appealRef;
    String appellant;
    String appellantRepresentative;
    String respondent;
    String hearingTime;
    String judiciary;
}
