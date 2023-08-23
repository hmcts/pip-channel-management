package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.CaseHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.CommonListHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.EtFortnightlyPressListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.Map;

@Service
public class EtFortnightlyPressListSummaryConverter implements ArtefactSummaryConverter {
    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        Map<String, Object> language =
            Map.of("rep", "Rep: ",
                   "noRep", "Rep: ");
        CommonListHelper.manipulatedListData(payload, Language.ENGLISH, true);
        EtFortnightlyPressListHelper.etFortnightlyListFormatted(payload, language);
        EtFortnightlyPressListHelper.splitByCourtAndDate(payload);
        return this.processEtFortnightlyPressList(payload);
    }

    private String processEtFortnightlyPressList(JsonNode node) {
        StringBuilder output = new StringBuilder();
        node.get("courtLists").forEach(
            courtList -> courtList.get("sittings").forEach(
                sitting -> sitting.get("hearing").forEach(
                    hearings -> hearings.forEach(
                        hearing -> hearing.get("case").forEach(hearingCase -> {
                            output.append('\n');
                            GeneralHelper.appendToStringBuilder(output, "Courtroom - ",
                                                                hearing, "courtRoom");
                            GeneralHelper.appendToStringBuilder(output, "Start Time - ",
                                                                sitting, "time");
                            output.append('\n');
                            String formattedDuration = "Duration - "
                                + CaseHelper.appendCaseSequenceIndicator(
                                GeneralHelper.findAndReturnNodeText(hearing, "formattedDuration"),
                                GeneralHelper.findAndReturnNodeText(hearingCase, "caseSequenceIndicator")
                            );
                            output.append(formattedDuration);

                            GeneralHelper.appendToStringBuilder(output, "Case Number - ",
                                                                hearingCase, "caseNumber");
                            GeneralHelper.appendToStringBuilder(output, "Claimant - ",
                                                                hearing,"claimant");
                            output.append(", ").append(hearing.get("claimantRepresentative").asText());
                            GeneralHelper.appendToStringBuilder(output, "Respondent - ",
                                                                hearing,"respondent");
                            output.append(", ").append(hearing.get("respondentRepresentative").asText());
                            GeneralHelper.appendToStringBuilder(output, "Hearing Type - ",
                                                                hearing,"hearingType");
                            GeneralHelper.appendToStringBuilder(output, "Jurisdiction - ",
                                                                hearingCase,"caseType");
                            GeneralHelper.appendToStringBuilder(output, "Hearing Platform - ",
                                                                hearing,"caseHearingChannel");
                        })
                    )
                )
            )
        );
        return output.toString();
    }
}
