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

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DailyCauseListHelper.preprocessArtefactForThymeLeafConverter;

public final class MagistratesStandardListHelper {
    public static final String COURT_LIST = "courtLists";
    public static final String CASE = "case";
    public static final String COURT_ROOM = "courtRoom";
    public static final String INDIVIDUAL_DETAILS = "individualDetails";
    public static final String AGE = "age";
    public static final String IN_CUSTODY = "inCustody";

    private MagistratesStandardListHelper() {
    }

    public static Context processArtefactForMagistratesStandardListThymeLeaf(
        JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) {
        Context context;
        context = preprocessArtefactForThymeLeafConverter(artefact, metadata, language, false);
        manipulatedMagistratesStandardList(artefact, language);
        context.setVariable("version", artefact.get("document").get("version").asText());
        return context;
    }

    public static void manipulatedMagistratesStandardList(JsonNode artefact, Map<String, Object> language) {
        ObjectMapper mapper = new ObjectMapper();
        artefact.get(COURT_LIST).forEach(courtList -> {
            courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM).forEach(courtRoom -> {
                courtRoom.get("session").forEach(session -> {
                    session.get("sittings").forEach(sitting -> {
                        manipulatedSitting(courtRoom, session, sitting);
                        ArrayNode allHearings = mapper.createArrayNode();
                        sitting.get("hearing").forEach(hearing -> {
                            if (hearing.has("party")) {
                                hearing.get("party").forEach(party -> {
                                    JsonNode cloneHearing = hearing.deepCopy();
                                    formatPartyInformation(cloneHearing, party, language);
                                    cloneHearing.get(CASE).forEach(thisCase -> {
                                        manipulatedCase(thisCase);
                                    });
                                    allHearings.add(cloneHearing);
                                });
                            }
                        });
                        ((ObjectNode) sitting).put("hearing", allHearings);
                    });
                });
            });
        });
    }

    private static void manipulatedSitting(JsonNode courtRoom, JsonNode session, JsonNode sitting) {
        String judiciary = DataManipulation.findAndManipulateJudiciary(sitting, false);
        String courtRoomName = GeneralHelper.findAndReturnNodeText(courtRoom, "courtRoomName");

        if (judiciary.isBlank()) {
            judiciary = DataManipulation.findAndManipulateJudiciary(session, false);

        }

        judiciary = courtRoomName.length() > 0 ? courtRoomName + ": " + judiciary : judiciary;
        ((ObjectNode) session).put("formattedSessionCourtRoom", judiciary);
        DateHelper.formatCaseTime(sitting, "sittingStart",
                                  "time", "h:mma");
    }

    private static void manipulatedCase(JsonNode thisCase) {
        ((ObjectNode) thisCase).put("formattedConvictionDate",
                            DateHelper.timeStampToBstTimeWithFormat(
                            GeneralHelper.findAndReturnNodeText(thisCase, "convictionDate"),
                            "dd/MM/yyyy"));
        ((ObjectNode) thisCase).put("formattedAdjournedDate",
                            DateHelper.timeStampToBstTimeWithFormat(
                            GeneralHelper.findAndReturnNodeText(thisCase, "adjournedDate"),
                            "dd/MM/yyyy"));

        if (!thisCase.has("caseSequenceIndicator")) {
            ((ObjectNode) thisCase).put("caseSequenceIndicator", "");
        }
    }

    private static void formatPartyInformation(JsonNode hearing, JsonNode party, Map<String, Object> language) {
        String defendant = "";

        if ("DEFENDANT".equals(PartyRoleMapper.convertPartyRole(party.get("partyRole").asText()))) {
            defendant = createIndividualDetails(party);
        }

        ((ObjectNode) hearing).put("defendantHeading", defendant);
        ((ObjectNode) hearing).put("defendantDateOfBirth", "");
        ((ObjectNode) hearing).put("defendantAddress", "");
        ((ObjectNode) hearing).put(AGE, "");
        ((ObjectNode) hearing).put("gender", "");
        ((ObjectNode) hearing).put("plea", "");
        ((ObjectNode) hearing).put(IN_CUSTODY, "");

        if (party.has(INDIVIDUAL_DETAILS)) {
            JsonNode individualDetails = party.get(INDIVIDUAL_DETAILS);

            formatDobAndAge(hearing, individualDetails, language);

            if (individualDetails.has("address")) {
                ((ObjectNode) hearing).put("defendantAddress",
                    formatDefendantAddress(individualDetails.get("address")));
            }

            ((ObjectNode) hearing).put("gender", "("
                + GeneralHelper.findAndReturnNodeText(individualDetails,
                    "gender") + ")");
            ((ObjectNode) hearing).put("plea",
                GeneralHelper.findAndReturnNodeText(individualDetails,
                    "plea"));

            if (individualDetails.has(IN_CUSTODY)
                && individualDetails.get(IN_CUSTODY).asBoolean()) {
                ((ObjectNode) hearing).put(IN_CUSTODY, "*");
            }
        }
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

    private static String formatDefendantAddress(JsonNode defendantAddress) {
        AtomicReference<String> formattedAddress = new AtomicReference<>("");
        defendantAddress.get("line").forEach(addressLine -> {
            formattedAddress.updateAndGet(v -> v
                + (addressLine.asText().length() > 0 ? ", "
                + addressLine.asText() : addressLine.asText()));
        });
        String town = GeneralHelper.findAndReturnNodeText(defendantAddress, "town");
        String county = GeneralHelper.findAndReturnNodeText(defendantAddress, "county");
        String postCode = GeneralHelper.findAndReturnNodeText(defendantAddress, "postCode");

        formattedAddress.updateAndGet(v -> v + (town.length() > 0 ? ", " + town : town));
        formattedAddress.updateAndGet(v -> v + (county.length() > 0 ? ", " + county : county));
        formattedAddress.updateAndGet(v -> v + (postCode.length() > 0 ? ", " + postCode : postCode));

        return formattedAddress.get();
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
