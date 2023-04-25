package uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist;

import lombok.Data;

@Data
public class Hearing {
    String hearingTime;
    String appealRef;
    String tribunalType;
    String appellant;
    String appellantRepresentative;
    String respondent;
    String judiciary;
}
