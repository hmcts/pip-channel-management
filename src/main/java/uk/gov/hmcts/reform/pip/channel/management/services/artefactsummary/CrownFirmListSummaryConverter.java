package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation.CrownFirmListHelper;

@Service
public class CrownFirmListSummaryConverter  implements ArtefactSummaryConverter {

    @Override
    public String convert(String payload) throws JsonProcessingException {
        JsonNode node = new ObjectMapper().readTree(payload);
        DataManipulation.manipulatedDailyListData(node, Language.ENGLISH, true);
        CrownFirmListHelper.crownFirmListFormatted(node);
        CrownFirmListHelper.splitByCourtAndDate(node);
        return this.processCrownFirmList(node);
    }

    private String processCrownFirmList(JsonNode node) {
        StringBuilder output = new StringBuilder();
        node.get("courtListsByDate").forEach(courtLists -> {
            courtLists.forEach(courtList -> {
                courtList.get("courtRooms").forEach(courtRoom -> {
                    courtRoom.get("hearings").forEach(hearings -> {
                        hearings.forEach(hearing -> {
                            hearing.get("case").forEach(hearingCase -> {
                                output.append('\n');
                                GeneralHelper.appendToStringBuilder(output, "Sitting at - ",
                                    hearingCase, "sittingAt");
                                GeneralHelper.appendToStringBuilder(output, "Case Reference - ",
                                    hearingCase, "caseReference");
                                GeneralHelper.appendToStringBuilder(output, "Defendant Name(s) - ",
                                    hearingCase, "defendant");
                                GeneralHelper.appendToStringBuilder(output, "Hearing Type - ",
                                    hearingCase, "hearingType");
                                output.append('\n');
                                checkCaseSequenceNo(output, hearingCase);
                                GeneralHelper.appendToStringBuilder(output, "Representative - ",
                                    hearingCase, "defendant_representative");
                                GeneralHelper.appendToStringBuilder(output, "Prosecuting Authority - ",
                                    hearingCase, "prosecuting_authority");
                                checkLinkedCasesAndListingNotes(output, hearingCase);
                            });
                        });
                    });
                });
            });
        });
        return output.toString();
    }

    private void checkCaseSequenceNo(StringBuilder output, JsonNode hearingCase) {
        String caseSequenceNo = "";
        if (!GeneralHelper.findAndReturnNodeText(hearingCase, "caseSequenceIndicator")
            .isEmpty()) {
            caseSequenceNo = " " + GeneralHelper.findAndReturnNodeText(
                hearingCase,"caseSequenceIndicator");
        }
        String formattedDuration = "Duration - "
            + GeneralHelper.findAndReturnNodeText(hearingCase, "formattedDuration")
            + caseSequenceNo;
        output.append(formattedDuration);
    }

    private static void checkLinkedCasesAndListingNotes(StringBuilder output, JsonNode hearingCase) {
        output.append('\n');

        if (!GeneralHelper.findAndReturnNodeText(hearingCase, "linkedCases").isEmpty()) {
            GeneralHelper.appendToStringBuilder(output, "Linked Cases - ", hearingCase, "linkedCases");
        }

        if (!GeneralHelper.findAndReturnNodeText(hearingCase, "listingNotes").isEmpty()) {
            GeneralHelper.appendToStringBuilder(output, "Listing Notes - ",
                                                hearingCase, "listingNotes"
            );
        }
    }
}
