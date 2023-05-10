package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleHelper;

public final class CrownDailyListHelper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String COURT_LIST = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String COURT_ROOM_NAME = "courtRoomName";

    private CrownDailyListHelper() {
    }

    public static void manipulatedCrownDailyListData(JsonNode artefact) {
        artefact.get(COURT_LIST).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(sitting -> {
                        DateHelper.formatStartTime(sitting, "h:mma", false);
                        sitting.get("hearing").forEach(hearing -> {
                            PartyRoleHelper.handleParties(hearing);
                            CrimeListHelper.formatCaseInformation(hearing);
                            CrimeListHelper.formatCaseHtmlTable(hearing);
                        });
                    })
                )
            )
        );
    }

    public static void findUnallocatedCases(JsonNode artefact) {
        ArrayNode unAllocatedCasesNodeArray = MAPPER.createArrayNode();
        artefact.get(COURT_LIST).forEach(courtList -> {
            final int[] roomCount = {0};
            courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(courtRoom -> {
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
            ObjectNode courtHouseObj = (ObjectNode) cloneCourtList.get(COURT_HOUSE);
            courtHouseObj.put("courtHouseName", "");
            courtHouseObj.put("courtHouseAddress", "");
            ((ObjectNode)cloneCourtList).put("unallocatedCases", true);
            courtHouseObj.putArray(COURT_ROOM).addAll(unAllocatedCasesNodeArray);

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
}
