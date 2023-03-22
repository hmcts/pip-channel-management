package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.ListType;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;

/**
 * Helper class for crime lists.
 *  Crown Daily List.
 *  Magistrates Public List.
 */
public final class CrimeListSummaryHelper {

    private static final String LISTING_NOTES = "listingNotes";
    private static final String LINKED_CASES = "linkedCases";

    private CrimeListSummaryHelper() {

    }

    /**
     * Takes in the Json node and the List type and returns a processed string for the correct list.
     * @param node The data to process.
     * @param listType The list the data is for.
     * @return A processed string for the correct list.
     */
    public static String processCrimeList(JsonNode node, ListType listType) {
        StringBuilder output = new StringBuilder();
        node.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(
                            hearing -> hearing.get("case").forEach(hearingCase -> {
                                output.append('\n');
                                GeneralHelper.appendToStringBuilder(output, "Sitting at - ",
                                                                    sitting, "time");
                                GeneralHelper.appendToStringBuilder(output, "Case Reference - ",
                                                                    hearingCase, "caseNumber");
                                GeneralHelper.appendToStringBuilder(output, "Defendant Name(s) - ",
                                                                    hearing, "defendant");
                                GeneralHelper.appendToStringBuilder(output, "Hearing Type - ",
                                                                    hearing, "hearingType");

                                if (ListType.CROWN_DAILY_LIST.equals(listType)) {
                                    processCrownDailyList(output, sitting, hearing, hearingCase);
                                } else if (ListType.MAGISTRATES_PUBLIC_LIST.equals(listType)) {
                                    processMagistratesPublicList(output, sitting, hearing, hearingCase);
                                }
                            })
                        )
                    )
                )
            )
        );
        return output.toString();
    }

    private static void processCrownDailyList(StringBuilder output, JsonNode sitting, JsonNode hearing,
                                              JsonNode hearingCase) {
        output.append('\n');
        String caseSequenceNo = "";
        if (!GeneralHelper.findAndReturnNodeText(hearingCase, LINKED_CASES)
            .isEmpty()) {
            caseSequenceNo = " " + GeneralHelper.findAndReturnNodeText(hearingCase,
                                                                       "caseSequenceIndicator");
        }
        String formattedDuration = "Duration - "
            + GeneralHelper.findAndReturnNodeText(sitting, "formattedDuration")
            + caseSequenceNo;
        output.append(formattedDuration);

        GeneralHelper.appendToStringBuilder(output, "Prosecuting Authority - ",
                                            hearing,"prosecutingAuthority");

        if (!GeneralHelper.findAndReturnNodeText(hearingCase, LINKED_CASES).isEmpty()) {
            GeneralHelper.appendToStringBuilder(output, "Linked Cases - ", hearingCase, LINKED_CASES);
        }

        if (!GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES).isEmpty()) {
            GeneralHelper.appendToStringBuilder(output, "Listing Notes - ",
                                                hearing, LISTING_NOTES
            );
        }
    }

    private static void processMagistratesPublicList(StringBuilder output, JsonNode sitting, JsonNode hearing,
                                                     JsonNode hearingCase) {

        GeneralHelper.appendToStringBuilder(output, "Prosecuting Authority - ", hearing,
                                            "prosecutingAuthority");
        output.append('\n');

        String caseSequenceNo = " " + GeneralHelper
            .findAndReturnNodeText(hearingCase, "caseSequenceIndicator");

        String formattedDuration = "Duration - "
            + GeneralHelper.findAndReturnNodeText(sitting, "formattedDuration")
            + caseSequenceNo;
        output.append(formattedDuration);

        if (!GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES).isEmpty()) {
            GeneralHelper.appendToStringBuilder(output, "Case Details - ",
                                                hearing, LISTING_NOTES
            );
        }
    }
}
