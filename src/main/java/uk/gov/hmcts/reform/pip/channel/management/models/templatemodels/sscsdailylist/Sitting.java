package uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist;

import lombok.Data;

import java.util.List;

@Data
public class Sitting {
    String sittingStart;
    List<Hearing> listOfHearings;
    String channel;
    String judiciary;
}
