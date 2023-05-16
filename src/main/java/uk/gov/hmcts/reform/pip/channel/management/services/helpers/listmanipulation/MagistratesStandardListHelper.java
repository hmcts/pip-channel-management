package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleMapper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.SittingHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"PMD.TooManyMethods"})
public final class MagistratesStandardListHelper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String COURT_LIST = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String CASE = "case";
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String AGE = "age";
    private static final String IN_CUSTODY = "inCustody";
    private static final String DEFENDANT_HEADING = "defendantHeading";
    private static final String PLEA = "plea";
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";
    private static final String FORMATTED_ADJOURNED_DATE = "formattedAdjournedDate";
    private static final String FORMATTED_CONVICTION_DATE = "formattedConvictionDate";

    private MagistratesStandardListHelper() {
    }

    public static void manipulatedMagistratesStandardList(JsonNode artefact, Map<String, Object> language) {
        artefact.get(COURT_LIST).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get("session").forEach(session -> {
                    ArrayNode allDefendants = MAPPER.createArrayNode();
                    session.get("sittings").forEach(sitting -> {
                        SittingHelper.manipulatedSitting(courtRoom, session, sitting,
                                        "formattedSessionCourtRoom");
                        DateHelper.formatStartTime(sitting, "h:mma", false);
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
                    ((ObjectNode) session).set("defendants", combineDefendantSittings(allDefendants));
                })
            )
        );
    }

    private static ArrayNode combineDefendantSittings(ArrayNode allDefendants) {
        AtomicReference<ObjectNode> defendantNode = new AtomicReference<>();
        List<String> uniqueDefendantNames = new ArrayList<>();
        ArrayNode defendantsPerSessions = MAPPER.createArrayNode();
        AtomicReference<ArrayNode> defendantInfo = new AtomicReference<>();
        allDefendants.forEach(df -> {
            if (!uniqueDefendantNames.contains(df.get(DEFENDANT_HEADING).asText())) {
                uniqueDefendantNames.add(df.get(DEFENDANT_HEADING).asText());
            }
        });

        uniqueDefendantNames.forEach(uniqueName -> {
            defendantNode.set(MAPPER.createObjectNode());
            defendantInfo.set(MAPPER.createArrayNode());
            int sittingSequence = 1;
            for (JsonNode defendant : allDefendants) {
                if (uniqueName.equals(defendant.get(DEFENDANT_HEADING).asText())) {
                    ((ObjectNode) defendant).put("sittingSequence", sittingSequence);
                    sittingSequence++;
                    defendantInfo.get().add(defendant);
                }
            }
            defendantNode.get().put(DEFENDANT_HEADING, uniqueName);
            defendantNode.get().set("defendantInfo", defendantInfo.get());
            defendantsPerSessions.add(defendantNode.get());
        });

        return defendantsPerSessions;
    }

    private static JsonNode manipulateHearingParty(JsonNode sitting, JsonNode hearing, JsonNode party,
                                                    Map<String, Object> language) {
        ObjectNode cloneHearing = hearing.deepCopy();
        formatPartyInformation(cloneHearing, party, language);
        cloneHearing.get(CASE).forEach(thisCase -> manipulatedCase(sitting, cloneHearing, thisCase));
        cloneHearing.put("time", GeneralHelper.findAndReturnNodeText(sitting, "time"));
        cloneHearing.put("formattedDuration",
                         GeneralHelper.findAndReturnNodeText(sitting, "formattedDuration"));
        findOffences(cloneHearing);

        return cloneHearing;
    }

    private static void findOffences(ObjectNode hearing) {
        ArrayNode allOffences = MAPPER.createArrayNode();
        hearing.get("offence").forEach(offence -> {
            ObjectNode offenceNode = MAPPER.createObjectNode();
            offenceNode.put("offenceTitle",
                GeneralHelper.findAndReturnNodeText(offence, "offenceTitle"));
            offenceNode.put(PLEA, GeneralHelper.findAndReturnNodeText(hearing, PLEA));
            offenceNode.put("dateOfPlea", "Need to confirm");
            offenceNode.put(FORMATTED_CONVICTION_DATE,
                GeneralHelper.findAndReturnNodeText(hearing, FORMATTED_CONVICTION_DATE));
            offenceNode.put(FORMATTED_ADJOURNED_DATE,
                GeneralHelper.findAndReturnNodeText(hearing, FORMATTED_ADJOURNED_DATE));
            offenceNode.put("offenceWording",
                GeneralHelper.findAndReturnNodeText(offence, "offenceWording"));
            allOffences.add(offenceNode);
        });
        hearing.set("allOffences", allOffences);
    }

    private static void manipulatedCase(JsonNode sitting, ObjectNode hearing, JsonNode thisCase) {
        hearing.put(FORMATTED_CONVICTION_DATE,
                    DateHelper.formatTimeStampToBst(GeneralHelper.findAndReturnNodeText(thisCase, "convictionDate"),
                                                    Language.ENGLISH, false, false, "dd/MM/yyyy"));
        hearing.put(FORMATTED_ADJOURNED_DATE,
                    DateHelper.formatTimeStampToBst(GeneralHelper.findAndReturnNodeText(thisCase, "adjournedDate"),
                                                    Language.ENGLISH, false, false, "dd/MM/yyyy"));
        hearing.put("prosecutionAuthorityCode",
                    GeneralHelper.findAndReturnNodeText(thisCase.get("informant"), "prosecutionAuthorityCode"));
        hearing.put("hearingNumber",
                    GeneralHelper.findAndReturnNodeText(thisCase, "hearingNumber"));
        hearing.put("caseHearingChannel",
                    GeneralHelper.findAndReturnNodeText(sitting, "caseHearingChannel"));
        hearing.put("caseNumber",
                    GeneralHelper.findAndReturnNodeText(thisCase, "caseNumber"));
        hearing.put("asn", "Need to confirm");
        hearing.put("panel", GeneralHelper.findAndReturnNodeText(thisCase, "panel"));

        hearing.put(CASE_SEQUENCE_INDICATOR, "");
        if (thisCase.has(CASE_SEQUENCE_INDICATOR)) {
            hearing.set(CASE_SEQUENCE_INDICATOR, thisCase.get(CASE_SEQUENCE_INDICATOR));
        }
    }

    private static void formatPartyInformation(ObjectNode hearing, JsonNode party, Map<String, Object> language) {
        if ("DEFENDANT".equals(PartyRoleMapper.convertPartyRole(party.get("partyRole").asText()))) {
            String defendant = PartyRoleHelper.createIndividualDetails(party);
            hearing.put(DEFENDANT_HEADING, defendant);
            hearing.put("defendantDateOfBirth", "");
            hearing.put("defendantAddress", "");
            hearing.put(AGE, "");
            hearing.put("gender", "");
            hearing.put(PLEA, "");
            hearing.put(IN_CUSTODY, "");

            if (party.has(INDIVIDUAL_DETAILS)) {
                JsonNode individualDetails = party.get(INDIVIDUAL_DETAILS);
                formatDobAndAge(hearing, individualDetails, language);

                hearing.put(DEFENDANT_HEADING, formatDefendantHeading(defendant, individualDetails));
                hearing.put("defendantAddress", formatDefendantAddress(individualDetails));
                hearing.put(PLEA, GeneralHelper.findAndReturnNodeText(individualDetails, PLEA));
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

    private static void formatDobAndAge(ObjectNode hearing, JsonNode individualDetails,
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

        hearing.put("defendantDateOfBirthAndAge",  dateOfBirthAndAge);
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
}
