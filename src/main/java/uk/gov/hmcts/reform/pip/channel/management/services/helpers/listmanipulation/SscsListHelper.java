package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.CourtHouse;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.CourtRoom;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.Hearing;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.Sitting;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SscsListHelper {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TIME_FORMAT = "h:mma";
    private static final String DELIMITER = ", ";

    private static final String CHANNEL = "channel";
    private static final String JUDICIARY = "judiciary";
    private static final String APPLICANT = "applicant";
    private static final String APPLICANT_REPRESENTATIVE = "applicantRepresentative";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String SESSION_CHANNEL = "sessionChannel";
    private static final String PARTY = "party";
    private static final String PARTY_ROLE = "partyRole";
    private static final String ORGANISATION_DETAILS = "organisationDetails";
    private static final String ORGANISATION_NAME = "organisationName";
    private static final String PROSECUTOR_ROLE = "PROSECUTOR";

    private SscsListHelper() {
    }

    public static CourtHouse courtHouseBuilder(JsonNode node) throws JsonProcessingException {
        JsonNode thisCourtHouseNode = node.get("courtHouse");
        CourtHouse thisCourtHouse = new CourtHouse();
        thisCourtHouse.setName(GeneralHelper.safeGet("courtHouseName", thisCourtHouseNode));
        thisCourtHouse.setPhone(GeneralHelper.safeGet("courtHouseContact.venueTelephone", thisCourtHouseNode));
        thisCourtHouse.setEmail(GeneralHelper.safeGet("courtHouseContact.venueEmail", thisCourtHouseNode));
        List<CourtRoom> courtRoomList = new ArrayList<>();
        for (JsonNode courtRoom : thisCourtHouseNode.get(COURT_ROOM)) {
            courtRoomList.add(courtRoomBuilder(courtRoom));
        }
        thisCourtHouse.setListOfCourtRooms(courtRoomList);
        return thisCourtHouse;
    }

    private static CourtRoom courtRoomBuilder(JsonNode node) throws JsonProcessingException {
        CourtRoom thisCourtRoom = new CourtRoom();
        thisCourtRoom.setName(GeneralHelper.safeGet("courtRoomName", node));
        List<Sitting> sittingList = new ArrayList<>();
        List<String> sessionChannel;
        TypeReference<List<String>> typeReference = new TypeReference<>() {
        };
        for (final JsonNode session : node.get(SESSION)) {
            sessionChannel = MAPPER.readValue(
                session.get(SESSION_CHANNEL).toString(),
                typeReference
            );
            String judiciary = formatJudiciary(session);
            String sessionChannelString = String.join(DELIMITER, sessionChannel);
            for (JsonNode sitting : session.get(SITTINGS)) {
                sittingList.add(sittingBuilder(sessionChannelString, sitting, judiciary));
            }
        }
        thisCourtRoom.setListOfSittings(sittingList);
        return thisCourtRoom;
    }

    private static Sitting sittingBuilder(String sessionChannel, JsonNode node, String judiciary)
        throws JsonProcessingException {
        Sitting sitting = new Sitting();
        DateHelper.formatStartTime(node, TIME_FORMAT, true);
        sitting.setJudiciary(judiciary);
        List<Hearing> listOfHearings = new ArrayList<>();
        if (node.has(CHANNEL)) {
            List<String> channelList = MAPPER.readValue(
                node.get(CHANNEL).toString(), new TypeReference<>() {
                });
            sitting.setChannel(String.join(DELIMITER, channelList));
        } else {
            sitting.setChannel(sessionChannel);
        }
        Iterator<JsonNode> nodeIterator = node.get(HEARING).elements();
        while (nodeIterator.hasNext()) {
            JsonNode currentHearingNode = nodeIterator.next();
            Hearing currentHearing = hearingBuilder(currentHearingNode);
            currentHearing.setHearingTime(node.get("time").asText());
            listOfHearings.add(currentHearing);
            currentHearing.setJudiciary(sitting.getJudiciary());
        }
        sitting.setListOfHearings(listOfHearings);
        return sitting;
    }

    /**
     * Format the judiciary into a comma seperated string.
     *
     * @param session The session containing the judiciary.
     * @return A string of the formatted judiciary.
     */
    private static String formatJudiciary(JsonNode session) {
        StringBuilder formattedJudiciaryBuilder = new StringBuilder();
        session.get(JUDICIARY).forEach(judiciary -> {
            if (formattedJudiciaryBuilder.length() > 0) {
                formattedJudiciaryBuilder.append(DELIMITER);
            }
            formattedJudiciaryBuilder
                .append(GeneralHelper.safeGet("johTitle", judiciary))
                .append(' ')
                .append(GeneralHelper.safeGet("johNameSurname", judiciary));
        });
        return formattedJudiciaryBuilder.toString();
    }

    private static Hearing hearingBuilder(JsonNode hearingNode) {
        Hearing currentHearing = new Hearing();
        PartyRoleHelper.findAndManipulatePartyInformation(hearingNode, false);
        currentHearing.setAppellant(hearingNode.get(APPLICANT).asText());
        currentHearing.setAppellantRepresentative(hearingNode.get(APPLICANT_REPRESENTATIVE).asText());
        currentHearing.setRespondent(formatRespondent(hearingNode));
        currentHearing.setAppealRef(GeneralHelper.safeGet("case.0.caseNumber", hearingNode));
        return currentHearing;
    }

    private static String formatRespondent(JsonNode hearingNode) {
        String informants = dealWithInformants(hearingNode);
        if (informants.isBlank()) {
            return getPartyProsecutors(hearingNode);
        }
        return informants;
    }

    private static String getPartyProsecutors(JsonNode hearingNode) {
        List<String> prosecutors = new ArrayList<>();

        for (JsonNode party : hearingNode.get(PARTY)) {
            String partyRole = GeneralHelper.findAndReturnNodeText(party, PARTY_ROLE);
            if (PROSECUTOR_ROLE.equals(partyRole) && party.has(ORGANISATION_DETAILS)) {
                String prosecutor = GeneralHelper.findAndReturnNodeText(
                    party.get(ORGANISATION_DETAILS), ORGANISATION_NAME
                );
                if (!prosecutor.isBlank()) {
                    prosecutors.add(prosecutor);
                }
            }
        }
        return String.join(DELIMITER, prosecutors);
    }

    private static String dealWithInformants(JsonNode node) {
        List<String> informants = new ArrayList<>();
        if (node.has("informant")) {
            GeneralHelper.safeGetNode("informant.0.prosecutionAuthorityRef", node).forEach(
                informant -> informants.add(informant.asText())
            );
        }
        return String.join(DELIMITER, informants);
    }
}
