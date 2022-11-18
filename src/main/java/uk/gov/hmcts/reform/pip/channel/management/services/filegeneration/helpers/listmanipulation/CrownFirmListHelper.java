package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.LocationHelper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DailyCauseListHelper.preprocessArtefactForThymeLeafConverter;

public final class CrownFirmListHelper {
    private static final String COURT_ROOM = "courtRoom";
    private static final String SITTING_DATE = "sittingDate";
    private static final String SITTINGS = "sittings";
    private static final String SITTING_START = "sittingStart";
    public static final String DEFENDANT = "defendant";
    public static final String PROSECUTING_AUTHORITY = "prosecuting_authority";
    private static final String LINKED_CASES = "linkedCases";
    private static final String LISTING_NOTES = "listingNotes";
    private static final String FORMATTED_COURT_ROOM_NAME = "formattedSessionCourtRoom";
    public static final String DEFENDANT_REPRESENTATIVE = "defendant_representative";
    public static final String COURT_LIST = "courtLists";
    public static final String FORMATTED_COURT_ROOM = "formattedCourtRoom";

    private CrownFirmListHelper() {
    }

    public static Context preprocessArtefactForCrownFirmListThymeLeafConverter(
        JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) {
        Context context;
        context = preprocessArtefactForThymeLeafConverter(artefact, metadata, language, true);
        crownFirmListFormatted(artefact);
        splitByCourtAndDate(artefact);
        return context;
    }

    private static List<String> findUniqueSittingDatesPerCounts(JsonNode artefact) {
        Map<Date, String> allSittingDateTimes = new ConcurrentHashMap<>();
        artefact.get(COURT_LIST).forEach(courtList -> {
            Map<Date, String> sittingDateTimes = EtFortnightlyPressListHelper.findAllSittingDates(
                courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM));
            allSittingDateTimes.putAll(sittingDateTimes);
        });
        return EtFortnightlyPressListHelper.findUniqueDateAndSort(allSittingDateTimes);
    }

    public static void splitByCourtAndDate(JsonNode artefact) {
        List<String> uniqueSittingDates = findUniqueSittingDatesPerCounts(artefact);
        String[] uniqueDates =  uniqueSittingDates.toArray(new String[0]);
        setListToDates(artefact, uniqueSittingDates);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode courtListByDateArray = mapper.createArrayNode();
        for (int i = 0; i < uniqueSittingDates.size(); i++) {
            int finalI = i;
            ArrayNode courtListArray = mapper.createArrayNode();
            artefact.get(COURT_LIST).forEach(courtList -> {

                ObjectNode courtListNode = mapper.createObjectNode();
                ArrayNode courtRoomsArray = mapper.createArrayNode();
                ObjectNode unAllocatedCourtRoom = mapper.createObjectNode();
                ArrayNode unAllocatedCourtRoomHearings = mapper.createArrayNode();

                courtListNode.put("courtName",
                    GeneralHelper.findAndReturnNodeText(courtList.get(LocationHelper.COURT_HOUSE),
                                                        "courtHouseName"));
                courtListNode.put("courtSittingDate", uniqueSittingDates.get(finalI));

                courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM).forEach(courtRoom -> {
                    ObjectNode courtRoomNode = mapper.createObjectNode();
                    ArrayNode hearingArray = mapper.createArrayNode();
                    courtRoom.get("session").forEach(session -> {
                        session.get(SITTINGS).forEach(sitting ->
                            EtFortnightlyPressListHelper.checkSittingDateAlreadyExists(
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
        ((ObjectNode)artefact).putArray("courtListsByDate").addAll(courtListByDateArray);
    }

    private static void checkAndAddToArrayNode(ArrayNode arrayToCheck, ObjectNode destinationNode,
                                               String destinationNodeAttribute, ArrayNode arrayToAdd) {
        if (arrayToCheck.size() > 0) {
            (destinationNode).putArray(destinationNodeAttribute).addAll(arrayToCheck);
            arrayToAdd.add(destinationNode);
        }
    }

    private static void checkToBeAllocatedRoom(ObjectNode courtRoomNode, JsonNode session,
        ObjectNode unAllocatedCourtRoom, ArrayNode hearingArray, ArrayNode unAllocatedCourtRoomHearings) {
        if (GeneralHelper.findAndReturnNodeText(session, FORMATTED_COURT_ROOM_NAME)
            .contains("to be allocated") && hearingArray.size() > 0) {
            (unAllocatedCourtRoom).put(
                FORMATTED_COURT_ROOM_NAME,
                GeneralHelper.findAndReturnNodeText(session, FORMATTED_COURT_ROOM)
            );
            (unAllocatedCourtRoom).put("unallocatedSection", "true");
            unAllocatedCourtRoomHearings.addAll(hearingArray);
        } else {
            (courtRoomNode).put(
                FORMATTED_COURT_ROOM_NAME,
                GeneralHelper.findAndReturnNodeText(session, FORMATTED_COURT_ROOM)
            );
            (courtRoomNode).put("unallocatedSection", "false");
        }
    }

    public static void crownFirmListFormatted(JsonNode artefact) {
        artefact.get(COURT_LIST).forEach(courtList -> {
            courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM).forEach(courtRoom -> {
                courtRoom.get("session").forEach(session -> {
                    session.get(SITTINGS).forEach(sitting -> {
                        String sittingDate = DateHelper.formatTimeStampToBstHavingWeekDay(
                            sitting.get(SITTING_START).asText(),
                            "dd MMMM yyyy", Language.ENGLISH);
                        ((ObjectNode)sitting).put(SITTING_DATE, sittingDate);
                        MagistratesStandardListHelper.manipulatedSitting(courtRoom, session, sitting,
                                                                         FORMATTED_COURT_ROOM);
                        sitting.get("hearing").forEach(hearing -> {
                            EtFortnightlyPressListHelper.formatCaseTime(sitting, hearing);
                            CrimeListHelper.findAndManipulatePartyInformation(hearing);
                            CrimeListHelper.formatCaseInformationCrownDaily(hearing);
                            CrimeListHelper.formatCaseHtmlTableCrownDailyList(hearing);
                            hearing.get("case").forEach(caseNode -> {
                                moveTableColumnValuesToHearing(sitting, hearing, caseNode);
                            });
                        });
                    });
                });
            });
        });
    }

    private static void moveTableColumnValuesToHearing(JsonNode sitting,
                                                       JsonNode hearing,
                                                       JsonNode caseNode) {
        ((ObjectNode)hearing).put("sittingAt",
            GeneralHelper.findAndReturnNodeText(sitting,"time"));
        ((ObjectNode)hearing).put("caseReference",
            GeneralHelper.findAndReturnNodeText(caseNode,"caseNumber"));
        ((ObjectNode)hearing).put(DEFENDANT,
            GeneralHelper.findAndReturnNodeText(hearing, DEFENDANT));
        ((ObjectNode)hearing).put("hearingType",
            GeneralHelper.findAndReturnNodeText(hearing,"hearingType"));
        ((ObjectNode)hearing).put("formattedDuration",
            GeneralHelper.findAndReturnNodeText(sitting,"formattedDuration"));
        ((ObjectNode)hearing).put("caseSequenceIndicator",
            GeneralHelper.findAndReturnNodeText(caseNode,"caseSequenceIndicator"));
        ((ObjectNode)hearing).put(DEFENDANT_REPRESENTATIVE,
            GeneralHelper.findAndReturnNodeText(hearing,DEFENDANT_REPRESENTATIVE));
        ((ObjectNode)hearing).put(PROSECUTING_AUTHORITY,
            GeneralHelper.findAndReturnNodeText(hearing, PROSECUTING_AUTHORITY));
        ((ObjectNode)hearing).put(LINKED_CASES,
            GeneralHelper.findAndReturnNodeText(caseNode, LINKED_CASES));
        ((ObjectNode)hearing).put(LISTING_NOTES,
            GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES));
        ((ObjectNode)hearing).put("caseCellBorder",
            GeneralHelper.findAndReturnNodeText(caseNode, "caseCellBorder"));
        ((ObjectNode)hearing).put("linkedCasesBorder",
            GeneralHelper.findAndReturnNodeText(caseNode, "linkedCasesBorder"));
    }

    private static void setListToDates(JsonNode artefact, List<String> uniqueSittingDates) {
        String startDate = uniqueSittingDates.get(0)
            .substring(uniqueSittingDates.get(0).indexOf(' ') + 1);
        String endDate = uniqueSittingDates.get(uniqueSittingDates.size() - 1)
            .substring(uniqueSittingDates.get(uniqueSittingDates.size() - 1).indexOf(' ') + 1);
        ((ObjectNode)artefact).put("listStartDate", startDate);
        ((ObjectNode)artefact).put("listEndDate", endDate);
    }
}
