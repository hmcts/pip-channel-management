package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.JudiciaryHelper;

import java.util.Iterator;
import java.util.List;

@Service
public class CivilDailyCauseListSummaryConverter implements ArtefactSummaryConverter {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";

    /**
     * Civil cause list parent method - iterates on courtHouse/courtList - if these need to be shown in further
     * iterations, do it here.
     *
     * @param payload - json body.
     * @return - string for output.
     * @throws JsonProcessingException - jackson req.
     */
    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        StringBuilder output = new StringBuilder();
        Iterator<JsonNode> courtHouseNode = payload.get(COURT_LISTS).elements();
        while (courtHouseNode.hasNext()) {
            JsonNode thisCourtHouse = courtHouseNode.next().get(COURT_HOUSE);
            output.append(processCivilDailyCourtRooms(thisCourtHouse)).append('\n');
        }
        return output.toString();
    }

    /**
     * court room iteration - cycles through courtrooms and deals with routing for hearing channel, judiciary
     * and sitting methods.
     *
     * @param node - jsonnode of courtrooms.
     * @return string with above-mentioned info.
     */
    private String processCivilDailyCourtRooms(JsonNode node) {
        Iterator<JsonNode> courtRoomNode = node.get(COURT_ROOM).elements();
        StringBuilder outputString = new StringBuilder();
        List<String> sessionChannel;
        TypeReference<List<String>> typeReference = new TypeReference<>() {
        };
        while (courtRoomNode.hasNext()) {
            JsonNode thisCourtRoom = courtRoomNode.next();
            JsonNode sessionChannelNode = thisCourtRoom.get(SESSION).get(0).path("sessionChannel");
            sessionChannel = MAPPER.convertValue(sessionChannelNode, typeReference);
            outputString.append("\n\nCourtroom: ").append(thisCourtRoom.get("courtRoomName").asText())
                .append(processCivilDailyJudiciary(thisCourtRoom))
                .append(processCivilDailySittings(thisCourtRoom, sessionChannel));
        }
        return outputString.toString();
    }

    /**
     * Judiciary iteration - gets known as field from judiciary node.
     *
     * @param node - node of judiciary.
     * @return judiciary string
     */
    private String processCivilDailyJudiciary(JsonNode node) {
        JsonNode judiciaryNode = node.get(SESSION).get(0).get("judiciary");
        if (judiciaryNode.isEmpty()) {
            return "";
        }
        return "\nJudiciary: " + JudiciaryHelper.findAndManipulateJudiciary(node.get(SESSION).get(0));
    }

    /**
     * sitting iteration class - deals with hearing channel, start time and hearing data (e.g. case names, refs etc)
     *
     * @param node           - node of sittings.
     * @param sessionChannel - session channel passed in from parent method - overridden if sitting level channel
     *                       exists.
     * @return string of these bits.
     */
    private String processCivilDailySittings(JsonNode node, List<String> sessionChannel) {
        JsonNode sittingNode = node.get(SESSION).get(0).get(SITTINGS);
        Iterator<JsonNode> sittingIterator = sittingNode.elements();
        StringBuilder outputString = new StringBuilder(26);
        int counter = 1;
        // below line is due to pmd "avoid using literals in conditional statements" rule.
        boolean sittingNodeSizeBool = sittingNode.size() > 1;
        while (sittingIterator.hasNext()) {
            outputString.append("\n•Hearing");
            if (sittingNodeSizeBool) {
                outputString.append(' ').append(counter);
                counter += 1;
            }
            JsonNode currentSitting = sittingIterator.next();
            DateHelper.formatStartTime(currentSitting, "h:mma");
            outputString.append(processCivilDailyHearings(currentSitting))
                .append("\nStart Time: ").append(currentSitting.get("time").asText())
                .append(processCivilDailyChannels(sessionChannel, currentSitting.path("channel")));
        }
        return outputString.toString();
    }

    /**
     * hearing channel handler. Sitting channel takes precedence over session channel if both exist (session channel
     * is mandatory, however).
     *
     * @param sessionChannel     - mentioned above.
     * @param currentSittingNode - node for getting current sitting channel data.
     * @return - string of correct channel.
     */
    private String processCivilDailyChannels(List<String> sessionChannel, JsonNode currentSittingNode) {
        StringBuilder outputString = new StringBuilder("\nHearing Channel: ");
        if (currentSittingNode.isMissingNode() || currentSittingNode.isEmpty()) {
            if (sessionChannel.isEmpty()) {
                return "";
            }
            for (String channel : sessionChannel) {
                outputString.append(channel);
            }
        } else {
            List<String> channelList = MAPPER.convertValue(currentSittingNode, new TypeReference<>() {
            });
            outputString.append(String.join(", ", channelList));
        }
        return outputString.toString();
    }

    /**
     * hearing iterator - gets case names, refs and hearing types.
     *
     * @param node - iterator of hearings.
     * @return String with that stuff in it.
     */
    private String processCivilDailyHearings(JsonNode node) {
        StringBuilder output = new StringBuilder(60);
        Iterator<JsonNode> hearingNode = node.get("hearing").elements();
        while (hearingNode.hasNext()) {
            JsonNode currentHearing = hearingNode.next();
            String hearingType = currentHearing.get("hearingType").asText();
            String caseName = currentHearing.get("case").get(0).get("caseName").asText();
            String caseNumber = currentHearing.get("case").get(0).get("caseNumber").asText();
            String caseType = GeneralHelper.findAndReturnNodeText(currentHearing.get("case").get(0), "caseType");
            output.append("\nCase Name: ").append(caseName)
                .append("\nCase Reference: ").append(caseNumber)
                .append("\nCase Type: ").append(caseType)
                .append("\nHearing Type: ").append(hearingType);
        }
        return output.toString();
    }
}
