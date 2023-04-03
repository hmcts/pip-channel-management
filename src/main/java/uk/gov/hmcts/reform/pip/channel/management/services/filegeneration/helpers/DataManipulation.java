package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.util.StringUtils;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.CourtHouse;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.CourtRoom;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.Hearing;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.Sitting;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"PMD.TooManyMethods"})
public final class DataManipulation {
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TIME_FORMAT = "h:mma";

    private static final String CHANNEL = "channel";
    private static final String JUDICIARY = "judiciary";
    private static final String APPLICANT = "applicant";
    private static final String RESPONDENT = "respondent";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String SESSION_CHANNEL = "sessionChannel";

    private DataManipulation() {
        throw new UnsupportedOperationException();
    }

    public static void manipulateCaseInformationForCop(JsonNode hearingCase) {
        if (!GeneralHelper.findAndReturnNodeText(hearingCase, CASE_SEQUENCE_INDICATOR).isEmpty()) {
            ((ObjectNode) hearingCase).put(
                "caseIndicator",
                hearingCase.get(CASE_SEQUENCE_INDICATOR).asText()
            );
        }
    }

    public static void manipulateCopListData(JsonNode artefact, Language language) {
        LocationHelper.formatRegionName(artefact);
        LocationHelper.formatRegionalJoh(artefact);

        artefact.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(session -> {
                    ((ObjectNode) session).put(
                        "formattedSessionJoh",
                        DataManipulation.findAndManipulateJudiciaryForCop(session)
                    );
                    session.get(SITTINGS).forEach(sitting -> {
                        DateHelper.calculateDuration(sitting, language);
                        DateHelper.formatStartTime(sitting, TIME_FORMAT, true);
                        DataManipulation.findAndConcatenateHearingPlatform(sitting, session);

                        sitting.get(HEARING).forEach(
                            hearing -> hearing.get("case").forEach(DataManipulation::manipulateCaseInformationForCop)
                        );
                    });
                })
            )
        );
    }

    public static void manipulatedDailyListData(JsonNode artefact, Language language, boolean initialised) {
        artefact.get("courtLists").forEach(
            courtList -> courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(session -> {
                    StringBuilder formattedJudiciary = new StringBuilder();
                    formattedJudiciary.append(findAndManipulateJudiciary(session));
                    session.get(SITTINGS).forEach(sitting -> {
                        DateHelper.calculateDuration(sitting, language);
                        DateHelper.formatStartTime(sitting, TIME_FORMAT, true);
                        findAndConcatenateHearingPlatform(sitting, session);

                        sitting.get(HEARING).forEach(hearing -> {
                            if (hearing.has("party")) {
                                PartyRoleHelper.findAndManipulatePartyInformation(hearing, language, initialised);
                            } else {
                                ((ObjectNode) hearing).put(APPLICANT, "");
                                ((ObjectNode) hearing).put(RESPONDENT, "");
                            }
                            hearing.get("case").forEach(DataManipulation::manipulateCaseInformation);
                        });
                    });
                    LocationHelper.formattedCourtRoomName(courtRoom, session, formattedJudiciary);
                })
            )
        );
    }

    public static void manipulateCaseInformation(JsonNode hearingCase) {
        if (!GeneralHelper.findAndReturnNodeText(hearingCase, CASE_SEQUENCE_INDICATOR).isEmpty()) {
            ((ObjectNode) hearingCase).put(
                "caseName",
                GeneralHelper.findAndReturnNodeText(hearingCase, "caseName")
                    + " " + hearingCase.get(CASE_SEQUENCE_INDICATOR).asText()
            );
        }

        if (!hearingCase.has("caseType")) {
            ((ObjectNode) hearingCase).put("caseType", "");
        }
    }

    public static void findAndConcatenateHearingPlatform(JsonNode sitting, JsonNode session) {
        StringBuilder formattedHearingPlatform = new StringBuilder();

        if (sitting.has(CHANNEL)) {
            GeneralHelper.loopAndFormatString(sitting, CHANNEL,
                                              formattedHearingPlatform, ", "
            );
        } else if (session.has(SESSION_CHANNEL)) {
            GeneralHelper.loopAndFormatString(session, SESSION_CHANNEL,
                                              formattedHearingPlatform, ", "
            );
        }

        ((ObjectNode) sitting).put("caseHearingChannel", GeneralHelper.trimAnyCharacterFromStringEnd(
            formattedHearingPlatform.toString().trim()));
    }

    public static String findAndManipulateJudiciaryForCop(JsonNode session) {
        StringBuilder formattedJudiciary = new StringBuilder();

        try {
            session.get(JUDICIARY).forEach(judiciary -> {
                if (formattedJudiciary.length() != 0) {
                    formattedJudiciary.append(", ");
                }

                formattedJudiciary.append(GeneralHelper.findAndReturnNodeText(judiciary, "johTitle"));
                formattedJudiciary.append(' ');
                formattedJudiciary.append(GeneralHelper.findAndReturnNodeText(judiciary, "johNameSurname"));
            });

        } catch (Exception ignored) {
            //No catch required, this is a valid scenario and makes the code cleaner than many if statements
        }

        return GeneralHelper.trimAnyCharacterFromStringEnd(formattedJudiciary.toString());
    }

    public static String findAndManipulateJudiciary(JsonNode judiciaryNode) {
        return findAndManipulateJudiciary(judiciaryNode, true);
    }

    public static String findAndManipulateJudiciary(JsonNode judiciaryNode, boolean addBeforeToJudgeName) {
        AtomicReference<StringBuilder> formattedJudiciary = new AtomicReference<>(new StringBuilder());
        AtomicReference<Boolean> foundPresiding = new AtomicReference<>(false);

        if (judiciaryNode.has(JUDICIARY)) {
            judiciaryNode.get(JUDICIARY).forEach(judiciary -> {
                if ("true".equals(GeneralHelper.findAndReturnNodeText(judiciary, "isPresiding"))) {
                    formattedJudiciary.set(new StringBuilder());
                    formattedJudiciary.get().append(GeneralHelper.findAndReturnNodeText(judiciary, "johKnownAs"));
                    foundPresiding.set(true);
                } else if (Boolean.FALSE.equals(foundPresiding.get())) {
                    String johKnownAs = GeneralHelper.findAndReturnNodeText(judiciary, "johKnownAs");
                    if (StringUtils.isNotBlank(johKnownAs)) {
                        formattedJudiciary.get()
                            .append(johKnownAs)
                            .append(", ");
                    }
                }
            });

            if (!GeneralHelper.trimAnyCharacterFromStringEnd(formattedJudiciary.toString()).isEmpty()
                    && addBeforeToJudgeName) {
                formattedJudiciary.get().insert(0, "Before: ");
            }
        }

        return GeneralHelper.trimAnyCharacterFromStringEnd(formattedJudiciary.toString());
    }

    private static Hearing hearingBuilder(JsonNode hearingNode, Language language) {
        Hearing currentHearing = new Hearing();
        PartyRoleHelper.findAndManipulatePartyInformation(hearingNode, language, false);
        currentHearing.setAppellant(hearingNode.get(APPLICANT).asText());
        currentHearing.setRespondent(dealWithInformants(hearingNode));
        currentHearing.setAppealRef(GeneralHelper.safeGet("case.0.caseNumber", hearingNode));
        return currentHearing;
    }

    private static String dealWithInformants(JsonNode node) {
        List<String> informants = new ArrayList<>();
        if (node.has("informant")) {
            GeneralHelper.safeGetNode("informant.0.prosecutionAuthorityRef", node).forEach(
                informant -> informants.add(informant.asText())
            );
        }
        return String.join(", ", informants);
    }

    private static Sitting sscsSittingBuilder(String sessionChannel, JsonNode node, String judiciary,
                                              Language language)
        throws JsonProcessingException {
        Sitting sitting = new Sitting();
        DateHelper.formatStartTime(node, TIME_FORMAT, true);
        sitting.setJudiciary(judiciary);
        List<Hearing> listOfHearings = new ArrayList<>();
        if (node.has(CHANNEL)) {
            List<String> channelList = MAPPER.readValue(
                node.get(CHANNEL).toString(), new TypeReference<>() {
                });
            sitting.setChannel(String.join(", ", channelList));
        } else {
            sitting.setChannel(sessionChannel);
        }
        Iterator<JsonNode> nodeIterator = node.get(HEARING).elements();
        while (nodeIterator.hasNext()) {
            JsonNode currentHearingNode = nodeIterator.next();
            Hearing currentHearing = hearingBuilder(currentHearingNode, language);
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
    private static String scssFormatJudiciary(JsonNode session) {
        StringBuilder formattedJudiciaryBuilder = new StringBuilder();
        session.get(JUDICIARY).forEach(judiciary -> {
            if (formattedJudiciaryBuilder.length() > 0) {
                formattedJudiciaryBuilder.append(", ");
            }
            formattedJudiciaryBuilder
                .append(GeneralHelper.safeGet("johTitle", judiciary))
                .append(' ')
                .append(GeneralHelper.safeGet("johNameSurname", judiciary));
        });
        return formattedJudiciaryBuilder.toString();
    }

    private static CourtRoom scssCourtRoomBuilder(JsonNode node, Language language) throws JsonProcessingException {
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
            String judiciary = scssFormatJudiciary(session);
            String sessionChannelString = String.join(", ", sessionChannel);
            for (JsonNode sitting : session.get(SITTINGS)) {
                sittingList.add(sscsSittingBuilder(sessionChannelString, sitting, judiciary, language));
            }
        }
        thisCourtRoom.setListOfSittings(sittingList);
        return thisCourtRoom;
    }

    public static CourtHouse courtHouseBuilder(JsonNode node, Language language) throws JsonProcessingException {
        JsonNode thisCourtHouseNode = node.get("courtHouse");
        CourtHouse thisCourtHouse = new CourtHouse();
        thisCourtHouse.setName(GeneralHelper.safeGet("courtHouseName", thisCourtHouseNode));
        thisCourtHouse.setPhone(GeneralHelper.safeGet("courtHouseContact.venueTelephone", thisCourtHouseNode));
        thisCourtHouse.setEmail(GeneralHelper.safeGet("courtHouseContact.venueEmail", thisCourtHouseNode));
        List<CourtRoom> courtRoomList = new ArrayList<>();
        for (JsonNode courtRoom : thisCourtHouseNode.get(COURT_ROOM)) {
            courtRoomList.add(scssCourtRoomBuilder(courtRoom, language));
        }
        thisCourtHouse.setListOfCourtRooms(courtRoomList);
        return thisCourtHouse;
    }
}
