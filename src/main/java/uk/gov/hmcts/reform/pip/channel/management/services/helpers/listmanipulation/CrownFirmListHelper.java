package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.SittingHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CrownFirmListHelper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SITTING_DATE = "sittingDate";
    private static final String SITTINGS = "sittings";
    private static final String SITTING_START = "sittingStart";
    private static final String DEFENDANT = "defendant";
    private static final String PROSECUTING_AUTHORITY = "prosecutingAuthority";
    private static final String LINKED_CASES = "linkedCases";
    private static final String LISTING_NOTES = "listingNotes";
    private static final String FORMATTED_COURT_ROOM_NAME = "formattedSessionCourtRoom";
    private static final String DEFENDANT_REPRESENTATIVE = "defendantRepresentative";
    private static final String COURT_LIST = "courtLists";
    private static final String FORMATTED_COURT_ROOM = "formattedCourtRoom";

    private CrownFirmListHelper() {
    }

    private static List<String> findUniqueSittingDatesPerCounts(JsonNode artefact) {
        Map<Date, String> allSittingDateTimes = new ConcurrentHashMap<>();
        artefact.get(COURT_LIST).forEach(courtList -> {
            Map<Date, String> sittingDateTimes = SittingHelper.findAllSittingDates(
                courtList.get(COURT_HOUSE).get(COURT_ROOM));
            allSittingDateTimes.putAll(sittingDateTimes);
        });
        return GeneralHelper.findUniqueDateAndSort(allSittingDateTimes);
    }

    public static void splitByCourtAndDate(JsonNode artefact) {
        List<String> uniqueSittingDates = findUniqueSittingDatesPerCounts(artefact);
        String[] uniqueDates =  uniqueSittingDates.toArray(new String[0]);
        setListToDates((ObjectNode) artefact, uniqueSittingDates);
        ArrayNode courtListByDateArray = MAPPER.createArrayNode();
        for (int i = 0; i < uniqueSittingDates.size(); i++) {
            int finalI = i;
            ArrayNode courtListArray = MAPPER.createArrayNode();
            artefact.get(COURT_LIST).forEach(courtList -> {

                ObjectNode courtListNode = MAPPER.createObjectNode();
                ArrayNode courtRoomsArray = MAPPER.createArrayNode();
                ObjectNode unAllocatedCourtRoom = MAPPER.createObjectNode();
                ArrayNode unAllocatedCourtRoomHearings = MAPPER.createArrayNode();

                courtListNode.put("courtName",
                    GeneralHelper.findAndReturnNodeText(courtList.get(COURT_HOUSE),
                                                        "courtHouseName"));
                courtListNode.put("courtSittingDate", uniqueSittingDates.get(finalI));

                courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(courtRoom -> {
                    ObjectNode courtRoomNode = MAPPER.createObjectNode();
                    ArrayNode hearingArray = MAPPER.createArrayNode();
                    courtRoom.get("session").forEach(session -> {
                        session.get(SITTINGS).forEach(sitting ->
                            SittingHelper.checkSittingDateAlreadyExists(
                            sitting, uniqueDates, hearingArray, finalI));

                        checkToBeAllocatedRoom(courtRoomNode, session, unAllocatedCourtRoom, hearingArray,
                                               unAllocatedCourtRoomHearings);
                    });
                    if (!GeneralHelper.findAndReturnNodeText(courtRoomNode, FORMATTED_COURT_ROOM_NAME).isBlank()) {
                        checkAndAddToArrayNode(hearingArray, courtRoomNode,
                                               "hearings", courtRoomsArray);
                    }
                });
                checkAndAddToArrayNode(unAllocatedCourtRoomHearings, unAllocatedCourtRoom,
                                       "hearings", courtRoomsArray);

                checkAndAddToArrayNode(courtRoomsArray, courtListNode,
                                       "courtRooms", courtListArray);
            });
            courtListByDateArray.add(courtListArray);
        }
        ((ObjectNode)artefact).putArray("courtListsByDate")
            .addAll(courtListByDateArray);
    }

    private static void checkAndAddToArrayNode(ArrayNode arrayToCheck, ObjectNode destinationNode,
                                               String destinationNodeAttribute, ArrayNode arrayToAdd) {
        if (arrayToCheck.size() > 0) {
            destinationNode.putArray(destinationNodeAttribute).addAll(arrayToCheck);
            arrayToAdd.add(destinationNode);
        }
    }

    private static void checkToBeAllocatedRoom(ObjectNode courtRoomNode, JsonNode session,
        ObjectNode unAllocatedCourtRoom, ArrayNode hearingArray, ArrayNode unAllocatedCourtRoomHearings) {
        if (GeneralHelper.findAndReturnNodeText(session, FORMATTED_COURT_ROOM_NAME)
            .contains("to be allocated") && hearingArray.size() > 0) {
            unAllocatedCourtRoom.put(
                FORMATTED_COURT_ROOM_NAME,
                GeneralHelper.findAndReturnNodeText(session, FORMATTED_COURT_ROOM)
            );
            unAllocatedCourtRoom.put("unallocatedSection", "true");
            unAllocatedCourtRoomHearings.addAll(hearingArray);
        } else {
            courtRoomNode.put(
                FORMATTED_COURT_ROOM_NAME,
                GeneralHelper.findAndReturnNodeText(session, FORMATTED_COURT_ROOM)
            );
            courtRoomNode.put("unallocatedSection", "false");
        }
    }

    public static void crownFirmListFormatted(JsonNode artefact) {
        artefact.get(COURT_LIST).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get(SITTINGS).forEach(
                        sitting -> {
                            String sittingDate = DateHelper.formatTimeStampToBst(
                                sitting.get(SITTING_START).asText(), Language.ENGLISH,
                                false, false, "EEEE dd MMMM yyyy");
                            ((ObjectNode)sitting).put(SITTING_DATE, sittingDate);
                            SittingHelper.manipulatedSitting(courtRoom, session, sitting, FORMATTED_COURT_ROOM);

                            sitting.get("hearing").forEach(hearing -> {
                                formatCaseTime(sitting, (ObjectNode) hearing);
                                PartyRoleHelper.handleParties(hearing);
                                CrimeListHelper.formatCaseInformation(hearing);
                                CrimeListHelper.formatCaseHtmlTable(hearing);
                                hearing.get("case").forEach(
                                    caseNode -> moveTableColumnValuesToHearing(sitting, hearing, caseNode)
                                );
                            });
                        }
                    )
                )
            )
        );
    }

    private static void moveTableColumnValuesToHearing(JsonNode sitting,
                                                       JsonNode hearing,
                                                       JsonNode caseNode) {
        ObjectNode hearingObj = (ObjectNode) hearing;
        hearingObj.put("sittingAt",
                       GeneralHelper.findAndReturnNodeText(hearing,"time"));
        hearingObj.put("caseReference",
                       GeneralHelper.findAndReturnNodeText(caseNode,"caseNumber"));
        hearingObj.put(DEFENDANT,
                       GeneralHelper.findAndReturnNodeText(hearing, DEFENDANT));
        hearingObj.put("hearingType",
                       GeneralHelper.findAndReturnNodeText(hearing,"hearingType"));
        hearingObj.put("formattedDuration",
                       GeneralHelper.findAndReturnNodeText(sitting,"formattedDuration"));
        hearingObj.put("caseSequenceIndicator",
                       GeneralHelper.findAndReturnNodeText(caseNode,"caseSequenceIndicator"));
        hearingObj.put(DEFENDANT_REPRESENTATIVE,
                       GeneralHelper.findAndReturnNodeText(hearing,DEFENDANT_REPRESENTATIVE));
        hearingObj.put(PROSECUTING_AUTHORITY,
                       GeneralHelper.findAndReturnNodeText(hearing, PROSECUTING_AUTHORITY));
        hearingObj.put(LINKED_CASES,
                       GeneralHelper.findAndReturnNodeText(caseNode, LINKED_CASES));
        hearingObj.put(LISTING_NOTES,
                       GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES));
        hearingObj.put("caseCellBorder",
                       GeneralHelper.findAndReturnNodeText(caseNode, "caseCellBorder"));
        hearingObj.put("linkedCasesBorder",
                       GeneralHelper.findAndReturnNodeText(caseNode, "linkedCasesBorder"));
    }

    private static void setListToDates(ObjectNode artefact, List<String> uniqueSittingDates) {
        String startDate = uniqueSittingDates.get(0)
            .substring(uniqueSittingDates.get(0).indexOf(' ') + 1);
        String endDate = uniqueSittingDates.get(uniqueSittingDates.size() - 1)
            .substring(uniqueSittingDates.get(uniqueSittingDates.size() - 1).indexOf(' ') + 1);
        artefact.put("listStartDate", startDate);
        artefact.put("listEndDate", endDate);
    }

    private static void formatCaseTime(JsonNode sitting, ObjectNode hearing) {
        if (!GeneralHelper.findAndReturnNodeText(sitting, SITTING_START).isEmpty()) {
            hearing.put("time",
                        DateHelper.formatTimeStampToBst(
                            GeneralHelper.findAndReturnNodeText(sitting, SITTING_START), Language.ENGLISH,
                            false, false, "h:mma"
                        ).replace(":00", ""));
        }
    }
}
