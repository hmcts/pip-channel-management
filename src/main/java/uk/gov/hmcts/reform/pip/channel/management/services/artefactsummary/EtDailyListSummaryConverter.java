package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.CaseHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.EtDailyListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

@Service
public class EtDailyListSummaryConverter implements ArtefactSummaryConverter {
    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        EtDailyListHelper.processRawListData(payload, Language.ENGLISH);

        StringBuilder output = new StringBuilder(140);
        payload.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(
                            hearing -> hearing.get("case").forEach(hearingCase -> {
                                GeneralHelper.appendToStringBuilderWithPrefix(output, "Start Time: ",
                                                                              sitting, "time", "\t•");

                                String formattedDuration = "\n\t\tDuration: "
                                    + CaseHelper.appendCaseSequenceIndicator(
                                        GeneralHelper.findAndReturnNodeText(sitting, "formattedDuration"),
                                        GeneralHelper.findAndReturnNodeText(hearingCase, "caseSequenceIndicator")
                                );
                                output.append(formattedDuration);

                                GeneralHelper.appendToStringBuilder(output, "Case Number: ",
                                                                    hearingCase, "caseNumber");

                                GeneralHelper.appendToStringBuilder(output, "Claimant: ",
                                                                    hearing, "claimant");
                                output.append(", Rep: ").append(hearing.get("claimantRepresentative").asText());

                                GeneralHelper.appendToStringBuilder(output, "Respondent: ",
                                                                    hearing, "respondent");
                                output.append(", Rep: ").append(hearing.get("respondentRepresentative").asText());

                                GeneralHelper.appendToStringBuilder(output, "Hearing Type: ",
                                                                    hearing, "hearingType");
                                GeneralHelper.appendToStringBuilder(output, "Jurisdiction: ",
                                                                    hearingCase, "caseType");
                                GeneralHelper.appendToStringBuilder(output, "Hearing Platform: ",
                                                                    sitting, "caseHearingChannel");
                                output.append('\n');
                            })
                        )
                    )
                )
            )
        );
        return output.toString();
    }
}
