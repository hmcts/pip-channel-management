package uk.gov.hmcts.reform.pip.channel.management.services.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class SittingHelper {
    private static final String SITTING_DATE = "sittingDate";
    private static final String SITTINGS = "sittings";
    private static final String SITTING_START = "sittingStart";
    private static final String CHANNEL = "channel";
    private static final String SESSION_CHANNEL = "sessionChannel";

    private SittingHelper() {
    }

    public static Map<Date, String> findAllSittingDates(JsonNode courtRooms) {
        Map<Date, String> sittingDateTimes = new ConcurrentHashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.UK);

        courtRooms.forEach(
            courtRoom -> courtRoom.get("session").forEach(
                session -> session.get(SITTINGS).forEach(sitting -> {
                    try {
                        sittingDateTimes.put(dateFormat.parse(sitting.get(SITTING_START).asText()),
                                             GeneralHelper.findAndReturnNodeText(sitting, SITTING_DATE));
                    } catch (ParseException e) {
                        log.error(e.getMessage());
                    }
                })
            )
        );

        return sittingDateTimes;
    }

    public static void checkSittingDateAlreadyExists(JsonNode sitting, String[] uniqueSittingDate,
                                                     ArrayNode hearingNodeArray, Integer sittingDateIndex) {
        String sittingDate = GeneralHelper.findAndReturnNodeText(sitting, SITTING_DATE);
        if (!sittingDate.isEmpty()
            && sittingDate.equals(uniqueSittingDate[sittingDateIndex])) {
            hearingNodeArray.add(sitting.get("hearing"));
        }
    }

    public static void manipulatedSitting(JsonNode courtRoom, JsonNode session, JsonNode sitting,
                                          String destinationNodeName) {
        String judiciary = JudiciaryHelper.findAndManipulateJudiciary(sitting, false);
        String courtRoomName = GeneralHelper.findAndReturnNodeText(courtRoom, "courtRoomName");

        if (judiciary.isBlank()) {
            judiciary = JudiciaryHelper.findAndManipulateJudiciary(session, false);

        }

        judiciary = courtRoomName.length() > 0 ? courtRoomName + ": " + judiciary : judiciary;
        ((ObjectNode) session).put(destinationNodeName, judiciary);
    }

    public static void findAndConcatenateHearingPlatform(JsonNode sitting, JsonNode session) {
        StringBuilder formattedHearingPlatform = new StringBuilder();

        if (sitting.has(CHANNEL)) {
            GeneralHelper.loopAndFormatString(sitting, CHANNEL, formattedHearingPlatform, ", ");
        } else if (session.has(SESSION_CHANNEL)) {
            GeneralHelper.loopAndFormatString(session, SESSION_CHANNEL, formattedHearingPlatform, ", ");
        }

        ((ObjectNode) sitting).put("caseHearingChannel", GeneralHelper.trimAnyCharacterFromStringEnd(
            formattedHearingPlatform.toString().trim()));
    }
}
