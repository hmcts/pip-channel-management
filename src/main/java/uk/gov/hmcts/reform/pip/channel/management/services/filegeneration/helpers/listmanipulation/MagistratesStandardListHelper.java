package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.PartyRoleMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DailyCauseListHelper.preprocessArtefactForThymeLeafConverter;

@SuppressWarnings({"PMD.TooManyMethods"})
public final class MagistratesStandardListHelper {
    public static final String COURT_LIST = "courtLists";
    public static final String CASE = "case";
    public static final String COURT_ROOM = "courtRoom";
    public static final String INDIVIDUAL_DETAILS = "individualDetails";
    public static final String AGE = "age";
    public static final String IN_CUSTODY = "inCustody";
    public static final String DEFENDANT_HEADING = "defendantHeading";
    public static final String PLEA = "plea";
    public static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";

    private MagistratesStandardListHelper() {
    }

    public static Context processArtefactForMagistratesStandardListThymeLeaf(
        JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) {
        Context context;
        context = preprocessArtefactForThymeLeafConverter(artefact, metadata, language, false);
        manipulatedMagistratesStandardList(artefact, language);
        context.setVariable("venueAddress", LocationHelper.formatFullVenueAddress(artefact));
        context.setVariable("version", artefact.get("document").get("version").asText());
        return context;
    }

    public static void manipulatedMagistratesStandardList(JsonNode artefact, Map<String, Object> language) {
        ObjectMapper mapper = new ObjectMapper();
        artefact.get(COURT_LIST).forEach(courtList -> {
            courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM).forEach(courtRoom -> {
                courtRoom.get("session").forEach(session -> {
                    ArrayNode allDefendants = mapper.createArrayNode();
                    session.get("sittings").forEach(sitting -> {
                        manipulatedSitting(courtRoom, session, sitting);
                        sitting.get("hearing").forEach(hearing -> {
                            if (hearing.has("party")) {
                                hearing.get("party").forEach(party -> {
                                    JsonNode cloneHearing =
                                        manipulateHearingParty(sitting, hearing, party, language);
                                    allDefendants.add(cloneHearing);
                                });
                            }
                        });
                    });
                    ((ObjectNode) session).put("defendants",
                                               combineDefendantSittings(allDefendants));
                });
            });
        });
    }

    private static ArrayNode combineDefendantSittings(ArrayNode allDefendants) {
        ObjectMapper mapper = new ObjectMapper();
        AtomicReference<ObjectNode> defendantNode = new AtomicReference<>();
        List<String> uniqueDefendantNames = new ArrayList<>();
        ArrayNode defendantsPerSessions = mapper.createArrayNode();
        AtomicReference<ArrayNode> defendantInfo = new AtomicReference<>();
        allDefendants.forEach(df -> {
            if (!uniqueDefendantNames.contains(df.get(DEFENDANT_HEADING).asText())) {
                uniqueDefendantNames.add(df.get(DEFENDANT_HEADING).asText());
            }
        });

        uniqueDefendantNames.forEach(uniqueName -> {
            defendantNode.set(mapper.createObjectNode());
            defendantInfo.set(mapper.createArrayNode());
            int sittingSequence = 1;
            for (JsonNode defendant : allDefendants) {
                if (uniqueName.equals(defendant.get(DEFENDANT_HEADING).asText())) {
                    ((ObjectNode) defendant).put("sittingSequence", sittingSequence);
                    sittingSequence++;
                    defendantInfo.get().add(defendant);
                }
            }
            defendantNode.get().put(DEFENDANT_HEADING, uniqueName);
            defendantNode.get().put("defendantInfo", defendantInfo.get());
            defendantsPerSessions.add(defendantNode.get());
        });

        return defendantsPerSessions;
    }

    private static JsonNode manipulateHearingParty(JsonNode sitting, JsonNode hearing, JsonNode party,
                                                    Map<String, Object> language) {
        JsonNode cloneHearing = hearing.deepCopy();
        formatPartyInformation(cloneHearing, party, language);
        cloneHearing.get(CASE).forEach(thisCase -> {
            manipulatedCase(sitting, cloneHearing, thisCase);
        });
        ((ObjectNode) cloneHearing).put("time",
                                        GeneralHelper.findAndReturnNodeText(sitting, "time"));
        ((ObjectNode) cloneHearing).put("formattedDuration",
                                        GeneralHelper.findAndReturnNodeText(sitting, "formattedDuration"));
        findOffences(cloneHearing);

        return cloneHearing;
    }

    private static void findOffences(JsonNode hearing) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode allOffences = mapper.createArrayNode();
        hearing.get("offence").forEach(offence -> {
            ObjectNode offenceNode = mapper.createObjectNode();
            ((ObjectNode) offenceNode).put("offenceTitle",
                GeneralHelper.findAndReturnNodeText(offence, "offenceTitle"));
            ((ObjectNode) offenceNode).put(PLEA,
                GeneralHelper.findAndReturnNodeText(hearing, PLEA));
            ((ObjectNode) offenceNode).put("dateOfPlea", "Need to confirm");
            ((ObjectNode) offenceNode).put("formattedConvictionDate",
                GeneralHelper.findAndReturnNodeText(hearing, "formattedConvictionDate"));
            ((ObjectNode) offenceNode).put("formattedAdjournedDate",
                GeneralHelper.findAndReturnNodeText(hearing, "formattedAdjournedDate"));
            ((ObjectNode) offenceNode).put("offenceWording",
                GeneralHelper.findAndReturnNodeText(offence, "offenceWording"));
            allOffences.add(offenceNode);
        });
        ((ObjectNode) hearing).put("allOffences", allOffences);
    }

    private static void manipulatedSitting(JsonNode courtRoom, JsonNode session, JsonNode sitting) {
        String judiciary = DataManipulation.findAndManipulateJudiciary(sitting, false);
        String courtRoomName = GeneralHelper.findAndReturnNodeText(courtRoom, "courtRoomName");

        if (judiciary.isBlank()) {
            judiciary = DataManipulation.findAndManipulateJudiciary(session, false);

        }

        judiciary = courtRoomName.length() > 0 ? courtRoomName + ": " + judiciary : judiciary;
        ((ObjectNode) session).put("formattedSessionCourtRoom", judiciary);
        DateHelper.formatStartTime(sitting, "h:mma");
    }

    private static void manipulatedCase(JsonNode sitting, JsonNode hearing, JsonNode thisCase) {
        ((ObjectNode) hearing).put("formattedConvictionDate",
            DateHelper.timeStampToBstTimeWithFormat(
            GeneralHelper.findAndReturnNodeText(thisCase, "convictionDate"),
            "dd/MM/yyyy"));
        ((ObjectNode) hearing).put("formattedAdjournedDate",
            DateHelper.timeStampToBstTimeWithFormat(
            GeneralHelper.findAndReturnNodeText(thisCase, "adjournedDate"),
            "dd/MM/yyyy"));
        ((ObjectNode) hearing).put("prosecutionAuthorityCode",
            GeneralHelper.findAndReturnNodeText(thisCase.get("informant"),
            "prosecutionAuthorityCode"));
        ((ObjectNode) hearing).put("hearingNumber",
            GeneralHelper.findAndReturnNodeText(thisCase,
            "hearingNumber"));
        ((ObjectNode) hearing).put("caseHearingChannel",
            GeneralHelper.findAndReturnNodeText(sitting,
            "caseHearingChannel"));
        ((ObjectNode) hearing).put("caseNumber",
            GeneralHelper.findAndReturnNodeText(thisCase,
            "caseNumber"));
        ((ObjectNode) hearing).put("asn", "Need to confirm");
        ((ObjectNode) hearing).put("panel",
            GeneralHelper.findAndReturnNodeText(thisCase,
            "panel"));
        ((ObjectNode) hearing).put(CASE_SEQUENCE_INDICATOR, "");
        if (thisCase.has(CASE_SEQUENCE_INDICATOR)) {
            ((ObjectNode) hearing).put(CASE_SEQUENCE_INDICATOR,
                thisCase.get(CASE_SEQUENCE_INDICATOR));
        }
    }

    private static void formatPartyInformation(JsonNode hearing, JsonNode party, Map<String, Object> language) {
        if ("DEFENDANT".equals(PartyRoleMapper.convertPartyRole(party.get("partyRole").asText()))) {
            String defendant = createIndividualDetails(party);
            ((ObjectNode) hearing).put(DEFENDANT_HEADING, defendant);
            ((ObjectNode) hearing).put("defendantDateOfBirth", "");
            ((ObjectNode) hearing).put("defendantAddress", "");
            ((ObjectNode) hearing).put(AGE, "");
            ((ObjectNode) hearing).put("gender", "");
            ((ObjectNode) hearing).put(PLEA, "");
            ((ObjectNode) hearing).put(IN_CUSTODY, "");

            if (party.has(INDIVIDUAL_DETAILS)) {
                JsonNode individualDetails = party.get(INDIVIDUAL_DETAILS);

                formatDobAndAge(hearing, individualDetails, language);

                ((ObjectNode) hearing).put(DEFENDANT_HEADING,
                    formatDefendantHeading(defendant, individualDetails));

                ((ObjectNode) hearing).put(
                    "defendantAddress",
                    formatDefendantAddress(individualDetails)
                );

                ((ObjectNode) hearing).put(
                    PLEA, GeneralHelper.findAndReturnNodeText(individualDetails, PLEA));
            }
        }
    }

    private static String formatDefendantHeading(String name, JsonNode individualDetails) {
        StringBuilder defendantName = new StringBuilder();
        defendantName.append(name);
        String gender = GeneralHelper.findAndReturnNodeText(individualDetails,"gender");
        String inCustody = "";

        if (!gender.isBlank()) {
            gender = " (" + gender + ")";
        }

        if (individualDetails.has(IN_CUSTODY)
            && individualDetails.get(IN_CUSTODY).asBoolean()) {
            inCustody = "*";
        }

        defendantName.append(gender).append(inCustody);
        return defendantName.toString();
    }

    private static void formatDobAndAge(JsonNode hearing, JsonNode individualDetails,
                                        Map<String, Object> language) {

        String dateOfBirth = GeneralHelper.findAndReturnNodeText(individualDetails,
                                                                 "dateOfBirth");
        String age = GeneralHelper.findAndReturnNodeText(individualDetails,
                                                         AGE);
        String dateOfBirthAndAge = "";
        if (!dateOfBirth.isBlank() && age.isBlank()) {
            dateOfBirthAndAge = dateOfBirth;
        } else if (dateOfBirth.isBlank() && !age.isBlank()) {
            dateOfBirthAndAge = language.get(AGE) + age;
        } else if (!dateOfBirth.isBlank() && !age.isBlank()) {
            dateOfBirthAndAge = dateOfBirth + " " + language.get(AGE) + age;
        }

        ((ObjectNode) hearing).put("defendantDateOfBirthAndAge",  dateOfBirthAndAge);

    }

    private static String formatDefendantAddress(JsonNode individualDetails) {
        if (individualDetails.has("address")) {
            JsonNode defendantAddress = individualDetails.get("address");
            StringBuilder address = new StringBuilder();
            for (JsonNode addressLine : defendantAddress.get("line")) {
                if (!addressLine.asText().isBlank()) {
                    String line = addressLine.asText() + ", ";
                    address.append(line);
                }
            }
            String town = GeneralHelper.findAndReturnNodeText(defendantAddress, "town");
            String county = GeneralHelper.findAndReturnNodeText(defendantAddress, "county");
            String postCode = GeneralHelper.findAndReturnNodeText(defendantAddress, "postCode");

            address.append(town)
                .append(county.length() > 0 ? ", " + county : county)
                .append(postCode.length() > 0 ? ", " + postCode : postCode);

            return address.toString();
        }
        return "";
    }

    private static String createIndividualDetails(JsonNode party) {
        if (party.has(INDIVIDUAL_DETAILS)) {
            JsonNode individualDetails = party.get(INDIVIDUAL_DETAILS);
            String forNames = GeneralHelper.findAndReturnNodeText(individualDetails, "individualForenames");
            String separator = " ";
            if (!forNames.isEmpty()) {
                separator = ", ";
            }
            return (GeneralHelper.findAndReturnNodeText(individualDetails, "individualSurname")
                + separator
                + forNames).trim();
        }
        return "";
    }
}
