package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleHelper;

public final class MagistratesPublicListHelper {
    private static final String COURT_LIST = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String CASE = "case";
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";
    private static final String LISTING_DETAILS = "listingDetails";
    private static final String LISTING_NOTES = "listingNotes";
    private static final String NO_BORDER_BOTTOM = "no-border-bottom";

    private MagistratesPublicListHelper() {
    }

    public static void manipulatedMagistratesPublicListData(JsonNode artefact) {
        artefact.get(COURT_LIST).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(sitting -> {
                        DateHelper.formatStartTime(sitting, "h:mma", false);
                        sitting.get("hearing").forEach(hearing -> {
                            PartyRoleHelper.handleParties(hearing);
                            formatCaseInformation(hearing);
                            formatCaseHtmlTable(hearing);
                        });
                    })
                )
            )
        );
    }

    private static void formatCaseInformation(JsonNode hearing) {
        StringBuilder listingNotes = new StringBuilder();

        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(cases -> {
                if (!cases.has(CASE_SEQUENCE_INDICATOR)) {
                    ((ObjectNode)cases).put(CASE_SEQUENCE_INDICATOR, "");
                }
            });
        }

        if (hearing.has(LISTING_DETAILS)) {
            listingNotes.append(hearing.get(LISTING_DETAILS).get("listingRepDeadline"));
            listingNotes.append(", ");
        }
        ((ObjectNode)hearing).put(LISTING_NOTES, GeneralHelper.trimAnyCharacterFromStringEnd(listingNotes.toString())
            .replace("\"", ""));
    }

    private static void formatCaseHtmlTable(JsonNode hearing) {
        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(cases -> {
                ObjectNode caseObj = (ObjectNode) cases;
                caseObj.put("bottomBorder", "");
                if (!GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES).isBlank()) {
                    caseObj.put("bottomBorder", NO_BORDER_BOTTOM);
                }
            });
        }
    }
}
