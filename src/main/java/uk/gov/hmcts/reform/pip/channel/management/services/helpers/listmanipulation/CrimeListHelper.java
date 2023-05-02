package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static uk.gov.hmcts.reform.pip.channel.management.services.helpers.CommonListHelper.preprocessArtefactForThymeLeafConverter;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CROWN_DAILY_LIST;

/**
 * Helper class for crime lists.
 *  Crown Daily List.
 *  Magistrates Public List.
 */
@SuppressWarnings({"PMD.TooManyMethods"})
public final class CrimeListHelper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final String PROSECUTING_AUTHORITY = "prosecutingAuthority";
    public static final String DEFENDANT = "defendant";
    public static final String DEFENDANT_REPRESENTATIVE = "defendantRepresentative";

    private static final String COURT_LIST = "courtLists";
    private static final String CASE = "case";
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

    public static Context preprocessArtefactForCrimeListsThymeLeafConverter(
        JsonNode artefact, Map<String, String> metadata, Map<String, Object> language, ListType listType) {
        Context context;
        context = preprocessArtefactForThymeLeafConverter(artefact, metadata, language, false);

        if (CROWN_DAILY_LIST.equals(listType)) {
            findUnallocatedCasesInCrownDailyListData(artefact);
        }

        manipulatedCrimeListData(artefact, listType);
        formattedCourtRoomName(artefact);
        context.setVariable("version", artefact.get("document").get("version").asText());
        return context;
    }

    public static void manipulatedCrimeListData(JsonNode artefact, ListType listType) {
        artefact.get(COURT_LIST).forEach(courtList ->
            courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM).forEach(courtRoom ->
                courtRoom.get("session").forEach(session ->
                    session.get("sittings").forEach(sitting -> {
                        DateHelper.formatStartTime(sitting, "h:mma", false);
                        sitting.get("hearing").forEach(hearing -> {
                            PartyRoleHelper.handleParties(hearing);
                            if (CROWN_DAILY_LIST.equals(listType)) {
                                formatCaseInformationCrownDaily(hearing);
                                formatCaseHtmlTableCrownDailyList(hearing);
                            } else if (ListType.MAGISTRATES_PUBLIC_LIST.equals(listType)) {
                                formatCaseInformationMagistratesPublic(hearing);
                                formatCaseHtmlTableMagistratesPublic(hearing);
                            }
                        });
                    })
                )
            )
        );
    }

    public static void findUnallocatedCasesInCrownDailyListData(JsonNode artefact) {
        ArrayNode unAllocatedCasesNodeArray = MAPPER.createArrayNode();
        artefact.get(COURT_LIST).forEach(courtList -> {
            final int[] roomCount = {0};
            courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM).forEach(courtRoom -> {
                if (GeneralHelper.findAndReturnNodeText(courtRoom, COURT_ROOM_NAME).contains("to be allocated")) {
                    JsonNode cloneCourtRoom = courtRoom.deepCopy();
                    unAllocatedCasesNodeArray.add(cloneCourtRoom);
                    ((ObjectNode)courtRoom).put("exclude", true);
                }
                roomCount[0]++;
            });
        });

        //IF THERE IS ANY UNALLOCATED CASES, ADD THE SECTION AT END OF COURTLIST ARRAY
        if (unAllocatedCasesNodeArray.size() > 0) {
            JsonNode cloneCourtList = artefact.get(COURT_LIST).get(0).deepCopy();
            ((ObjectNode)cloneCourtList.get(LocationHelper.COURT_HOUSE)).put("courtHouseName", "");
            ((ObjectNode)cloneCourtList.get(LocationHelper.COURT_HOUSE)).put("courtHouseAddress", "");
            ((ObjectNode)cloneCourtList).put("unallocatedCases", true);
            ((ObjectNode)cloneCourtList.get(LocationHelper.COURT_HOUSE))
                .putArray(COURT_ROOM).addAll(unAllocatedCasesNodeArray);

            ArrayNode courtListArray = MAPPER.createArrayNode();

            if (artefact.get(COURT_LIST).isArray()) {
                for (final JsonNode courtList : artefact.get(COURT_LIST)) {
                    ((ObjectNode)courtList).put("unallocatedCases", false);
                    courtListArray.add(courtList);
                }
                courtListArray.add(cloneCourtList);
                ((ObjectNode)artefact).putArray(COURT_LIST).addAll(courtListArray);
            }
        }
    }

    public static void formattedCourtRoomName(JsonNode artefact) {
        artefact.get(COURT_LIST).forEach(
            courtList -> courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> {
                        if (GeneralHelper.findAndReturnNodeText(courtRoom, COURT_ROOM_NAME)
                            .contains("to be allocated")) {
                            ((ObjectNode) session).put(
                                SESSION_COURT_ROOM,
                                GeneralHelper.findAndReturnNodeText(courtRoom, COURT_ROOM_NAME)
                            );
                        } else {
                            ((ObjectNode) session).put(
                                SESSION_COURT_ROOM,
                                GeneralHelper.findAndReturnNodeText(session, SESSION_COURT_ROOM)
                                    .replace("Before: ", "")
                            );
                        }
                    })
            )
        );
    }

    private static void formatCaseInformationMagistratesPublic(JsonNode hearing) {
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



    public static void formatCaseInformationCrownDaily(JsonNode hearing) {
        AtomicReference<StringBuilder> linkedCases = new AtomicReference<>(new StringBuilder());
        StringBuilder listingNotes = new StringBuilder();

        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(cases -> {
                linkedCases.set(new StringBuilder());
                if (!cases.has("caseNumber")) {
                    ((ObjectNode) cases).put("caseNumber", "");
                }

                if (cases.has("caseLinked")) {
                    cases.get("caseLinked").forEach(
                        caseLinked -> linkedCases.get()
                            .append(GeneralHelper.findAndReturnNodeText(caseLinked, "caseId")).append(", ")
                    );
                }
                ((ObjectNode) cases).put(
                    LINKED_CASES,
                    GeneralHelper.trimAnyCharacterFromStringEnd(linkedCases.toString())
                );

                if (!cases.has(CASE_SEQUENCE_INDICATOR)) {
                    ((ObjectNode) cases).put(CASE_SEQUENCE_INDICATOR, "");
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

    private static void formatCaseHtmlTableMagistratesPublic(JsonNode hearing) {
        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(cases -> {
                ((ObjectNode)cases).put("bottomBorder", "");
                if (!GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES).isBlank()) {
                    ((ObjectNode)cases).put("bottomBorder", NO_BORDER_BOTTOM);
                }
            });
        }
    }

    public static void formatCaseHtmlTableCrownDailyList(JsonNode hearing) {
        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(cases -> {
                ((ObjectNode)cases).put("caseCellBorder", "");
                if (!GeneralHelper.findAndReturnNodeText(cases, LINKED_CASES).isEmpty()
                    || !GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES).isEmpty()) {
                    ((ObjectNode)cases).put("caseCellBorder", NO_BORDER_BOTTOM);
                }

                ((ObjectNode)cases).put("linkedCasesBorder", "");
                if (!GeneralHelper.findAndReturnNodeText(cases, LINKED_CASES).isEmpty()) {
                    ((ObjectNode)cases).put("linkedCasesBorder", NO_BORDER_BOTTOM);
                }
            });
        }
    }
}
