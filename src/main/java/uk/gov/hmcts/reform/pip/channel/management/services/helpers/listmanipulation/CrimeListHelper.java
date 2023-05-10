package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Helper class for crime lists.
 *  Crown Daily List.
 *  Magistrates Public List.
 */
public final class CrimeListHelper {
    private static final String COURT_LIST = "courtLists";
    private static final String CASE = "case";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";
    private static final String LISTING_DETAILS = "listingDetails";
    private static final String LISTING_NOTES = "listingNotes";
    private static final String LINKED_CASES = "linkedCases";
    private static final String COURT_ROOM_NAME = "courtRoomName";

    private static final String SESSION_COURT_ROOM = "formattedSessionCourtRoom";
    private static final String NO_BORDER_BOTTOM = "no-border-bottom";

    private CrimeListHelper() {
    }

    public static void formattedCourtRoomName(JsonNode artefact) {
        artefact.get(COURT_LIST).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> {
                        ObjectNode sessionObj = (ObjectNode) session;
                        if (GeneralHelper.findAndReturnNodeText(courtRoom, COURT_ROOM_NAME)
                            .contains("to be allocated")) {
                            sessionObj.put(
                                SESSION_COURT_ROOM,
                                GeneralHelper.findAndReturnNodeText(courtRoom, COURT_ROOM_NAME)
                            );
                        } else {
                            sessionObj.put(
                                SESSION_COURT_ROOM,
                                GeneralHelper.findAndReturnNodeText(session, SESSION_COURT_ROOM)
                                    .replace("Before: ", "")
                            );
                        }
                    })
            )
        );
    }

    public static void formatCaseInformation(JsonNode hearing) {
        AtomicReference<StringBuilder> linkedCases = new AtomicReference<>(new StringBuilder());
        StringBuilder listingNotes = new StringBuilder();

        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(hearingCase -> {
                linkedCases.set(new StringBuilder());
                ObjectNode caseObj = (ObjectNode) hearingCase;

                if (!hearingCase.has("caseNumber")) {
                    caseObj.put("caseNumber", "");
                }

                if (hearingCase.has("caseLinked")) {
                    hearingCase.get("caseLinked").forEach(
                        caseLinked -> linkedCases.get()
                            .append(GeneralHelper.findAndReturnNodeText(caseLinked, "caseId")).append(", ")
                    );
                }
                caseObj.put(LINKED_CASES, GeneralHelper.trimAnyCharacterFromStringEnd(linkedCases.toString()));

                if (!hearingCase.has(CASE_SEQUENCE_INDICATOR)) {
                    caseObj.put(CASE_SEQUENCE_INDICATOR, "");
                }
            });
        }

        if (hearing.has(LISTING_DETAILS)) {
            listingNotes.append(hearing.get(LISTING_DETAILS).get("listingRepDeadline"));
            listingNotes.append(", ");
        }
        ((ObjectNode) hearing).put(LISTING_NOTES,
                                   GeneralHelper.trimAnyCharacterFromStringEnd(listingNotes.toString())
                                       .replace("\"", "")
        );
    }

    public static void formatCaseHtmlTable(JsonNode hearing) {
        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(hearingCase -> {
                ObjectNode caseObj = (ObjectNode) hearingCase;
                (caseObj).put("caseCellBorder", "");
                if (!GeneralHelper.findAndReturnNodeText(hearingCase, LINKED_CASES).isEmpty()
                    || !GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES).isEmpty()) {
                    caseObj.put("caseCellBorder", NO_BORDER_BOTTOM);
                }

                caseObj.put("linkedCasesBorder", "");
                if (!GeneralHelper.findAndReturnNodeText(hearingCase, LINKED_CASES).isEmpty()) {
                    caseObj.put("linkedCasesBorder", NO_BORDER_BOTTOM);
                }
            });
        }
    }
}
