package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.CaseHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.CommonListHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.CrownFirmListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

@Service
public class CrownFirmListSummaryConverter  implements ArtefactSummaryConverter {
    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        CommonListHelper.manipulatedListData(payload, Language.ENGLISH, true);
        if (GeneralHelper.hearingHasParty(payload)) {
            CrownFirmListHelper.crownFirmListFormattedV1(payload, Language.ENGLISH);
            CrownFirmListHelper.splitByCourtAndDateV1(payload);
            return this.processCrownFirmListV1(payload);
        }
        CrownFirmListHelper.crownFirmListFormatted(payload, Language.ENGLISH);
        CrownFirmListHelper.splitByCourtAndDate(payload);
        return this.processCrownFirmList(payload);
    }

    private String processCrownFirmList(JsonNode node) {
        StringBuilder output = new StringBuilder();
        node.get("courtListsByDate").forEach(
            courtLists -> courtLists.forEach(
                courtList -> courtList.get("courtRooms").forEach(
                    courtRoom -> courtRoom.get("hearings").forEach(
                        hearings -> hearings.forEach(
                            hearing -> hearing.get("case").forEach(hearingCase -> {
                                output.append('\n');
                                GeneralHelper.appendToStringBuilder(output, "Sitting at - ",
                                                                    hearingCase, "sittingAt");
                                GeneralHelper.appendToStringBuilder(output, "Case Reference - ",
                                                                    hearingCase, "caseReference");
                                GeneralHelper.appendToStringBuilder(output, "Defendant Name(s) - ",
                                                                    hearingCase, "defendant");
                                GeneralHelper.appendToStringBuilder(output, "Hearing Type - ",
                                                                    hearingCase, "hearingType");
                                output.append("\nDuration - ")
                                    .append(CaseHelper.appendCaseSequenceIndicator(
                                        GeneralHelper.findAndReturnNodeText(hearingCase, "formattedDuration"),
                                        GeneralHelper.findAndReturnNodeText(hearingCase, "caseSequenceIndicator")
                                    ));

                                GeneralHelper.appendToStringBuilder(output, "Representative - ",
                                                                    hearingCase, "defendantRepresentative");
                                GeneralHelper.appendToStringBuilder(output, "Prosecuting Authority - ",
                                                                    hearingCase, "prosecutingAuthority");
                                checkLinkedCasesAndListingNotes(output, hearingCase);
                            })
                        )
                    )
                )
            )
        );
        return output.toString();
    }

    @Deprecated
    private String processCrownFirmListV1(JsonNode node) {
        StringBuilder output = new StringBuilder();
        node.get("courtListsByDate").forEach(
            courtLists -> courtLists.forEach(
                courtList -> courtList.get("courtRooms").forEach(
                    courtRoom -> courtRoom.get("hearings").forEach(
                        hearings -> hearings.forEach(hearing -> {
                            output.append('\n');
                            GeneralHelper.appendToStringBuilder(output, "Sitting at - ",
                                                                hearing, "sittingAt");
                            GeneralHelper.appendToStringBuilder(output, "Case Reference - ",
                                                                hearing, "caseReference");
                            GeneralHelper.appendToStringBuilder(output, "Defendant Name(s) - ",
                                                                hearing, "defendant");
                            GeneralHelper.appendToStringBuilder(output, "Hearing Type - ",
                                                                hearing, "hearingType");
                            output.append("\nDuration - ")
                                .append(CaseHelper.appendCaseSequenceIndicator(
                                    GeneralHelper.findAndReturnNodeText(hearing, "formattedDuration"),
                                    GeneralHelper.findAndReturnNodeText(hearing, "caseSequenceIndicator")
                                ));

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

    private static void checkLinkedCasesAndListingNotes(StringBuilder output, JsonNode node) {
        if (!GeneralHelper.findAndReturnNodeText(node, "linkedCases").isEmpty()) {
            GeneralHelper.appendToStringBuilder(output, "Linked Cases - ", node, "linkedCases");
        }

        if (!GeneralHelper.findAndReturnNodeText(node, "listingNotes").isEmpty()) {
            GeneralHelper.appendToStringBuilder(output, "Listing Notes - ", node, "listingNotes");
        }
    }
}
