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
        node.get("courtListsByDate").forEach(courtLists ->
            courtLists.forEach(courtList ->
                courtList.get("courtRooms").forEach(courtRoom ->
                    courtRoom.get("hearings").forEach(hearings ->
                        hearings.forEach(hearing -> {
                            output.append('\n');
                            GeneralHelper.appendToStringBuilder(output, "Sitting at - ",
                                                                hearing, "sittingAt");
                            GeneralHelper.appendToStringBuilder(output, "Case Reference - ",
                                                                hearing, "caseReference");
                            GeneralHelper.appendToStringBuilder(output, "Defendant Name(s) - ",
                                                                hearing, "defendant");
                            GeneralHelper.appendToStringBuilder(output, "Hearing Type - ",
                                                                hearing, "hearingType");
                            output.append('\n');
                            checkCaseSequenceNo(output, hearing);
                            GeneralHelper.appendToStringBuilder(output, "Representative - ",
                                                                hearing, "defendantRepresentative");
                            GeneralHelper.appendToStringBuilder(output, "Prosecuting Authority - ",
                                                                hearing, "prosecutingAuthority");
                            checkLinkedCasesAndListingNotes(output, hearing);
                        })
                     )
                )
            )
        );
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
