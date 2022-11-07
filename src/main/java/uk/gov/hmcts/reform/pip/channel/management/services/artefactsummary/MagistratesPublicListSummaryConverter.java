package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation.MagistratesPublicListHelper;

@Service
public class MagistratesPublicListSummaryConverter implements ArtefactSummaryConverter {

    /**
     * Magistrates public lists - iterates on courtHouse/courtList.
     *
     * @param payload - json body.
     * @return - string for output.
     * @throws JsonProcessingException - jackson req.
     */
    @Override
    public String convert(String payload) throws JsonProcessingException {
        JsonNode node = new ObjectMapper().readTree(payload);

        DataManipulation.manipulatedDailyListData(node, Language.ENGLISH, false);
        MagistratesPublicListHelper.manipulatedMagistratesPublicListData(node);

        return this.processMagistratesPublicList(node);
    }

    private String processMagistratesPublicList(JsonNode node) {
        StringBuilder output = new StringBuilder();
        node.get("courtLists").forEach(courtList -> {
            courtList.get("courtHouse").get("courtRoom").forEach(courtRoom -> {
                courtRoom.get("session").forEach(session -> {
                    session.get("sittings").forEach(sitting -> {
                        sitting.get("hearing").forEach(hearing -> {
                            hearing.get("case").forEach(hearingCase -> {

                                output.append('\n');

                                GeneralHelper.appendToStringBuilder(output, "Sitting at - ",
                                                                    sitting, "time");
                                GeneralHelper.appendToStringBuilder(output, "Case Reference - ",
                                                                    hearingCase, "caseNumber");
                                GeneralHelper.appendToStringBuilder(output, "Defendant Name(s) - ",
                                                                    hearing, "defendant");
                                GeneralHelper.appendToStringBuilder(output, "Hearing Type - ",
                                                                    hearing, "hearingType");
                                GeneralHelper.appendToStringBuilder(output, "Prosecuting Authority - ",
                                                                    hearing,"prosecuting_authority");
                                output.append('\n');
                                formatDuration(output, sitting, hearingCase);
                                if (!GeneralHelper.findAndReturnNodeText(hearing, "listingNotes").isEmpty()) {
                                    GeneralHelper.appendToStringBuilder(output, "Case Details - ",
                                                                        hearing, "listingNotes"
                                    );
                                }
                            });
                        });
                    });
                });
            });
        });

        return output.toString();
    }

    private void formatDuration(StringBuilder output, JsonNode sitting, JsonNode hearingCase) {
        String caseSequenceNo = " " + GeneralHelper.findAndReturnNodeText(hearingCase,
                                                                   "caseSequenceIndicator");
        String formattedDuration = "Duration - "
            + GeneralHelper.findAndReturnNodeText(sitting, "formattedDuration")
            + caseSequenceNo;
        output.append(formattedDuration);
    }
}
