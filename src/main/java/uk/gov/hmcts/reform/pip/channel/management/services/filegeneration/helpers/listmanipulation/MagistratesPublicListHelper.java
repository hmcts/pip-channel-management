package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.PartyRoleMapper;

import java.util.Map;

import static uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DailyCauseListHelper.preprocessArtefactForThymeLeafConverter;

public final class MagistratesPublicListHelper {
    public static final String PROSECUTING_AUTHORITY = "prosecuting_authority";
    public static final String DEFENDANT = "defendant";
    public static final String COURT_LIST = "courtLists";
    public static final String CASE = "case";
    public static final String COURT_ROOM = "courtRoom";

    private MagistratesPublicListHelper() {
    }

    public static Context preprocessArtefactForMagistratesPublicListThymeLeafConverter(
        JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) {
        Context context;
        context = preprocessArtefactForThymeLeafConverter(artefact, metadata, language, false);
        manipulatedMagistratesPublicListData(artefact);
        formattedCourtRoomName(artefact);
        context.setVariable("version", artefact.get("document").get("version").asText());
        return context;
    }

    public static void manipulatedMagistratesPublicListData(JsonNode artefact) {
        artefact.get(COURT_LIST).forEach(courtList -> {
            courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM).forEach(courtRoom -> {
                courtRoom.get("session").forEach(session -> {
                    session.get("sittings").forEach(sitting -> {
                        formatCaseTime(sitting);
                        sitting.get("hearing").forEach(hearing -> {
                            if (hearing.has("party")) {
                                findAndManipulatePartyInformation(hearing);
                            } else {
                                ((ObjectNode) hearing).put(PROSECUTING_AUTHORITY, "");
                                ((ObjectNode) hearing).put(DEFENDANT, "");
                            }
                            formatCaseInformation(hearing);
                            formatCaseHtmlTable(hearing);
                        });
                    });
                });
            });
        });
    }

    public static void formattedCourtRoomName(JsonNode artefact) {
        artefact.get(COURT_LIST).forEach(courtList -> {
            courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM).forEach(courtRoom -> {
                courtRoom.get("session").forEach(session -> {
                    if (GeneralHelper.findAndReturnNodeText(courtRoom, "courtRoomName")
                        .contains("to be allocated")) {
                        ((ObjectNode)session).put("formattedSessionCourtRoom",
                            GeneralHelper.findAndReturnNodeText(courtRoom, "courtRoomName"));
                    } else {
                        ((ObjectNode)session).put("formattedSessionCourtRoom",
                            GeneralHelper.findAndReturnNodeText(session, "formattedSessionCourtRoom")
                            .replace("Before: ", ""));
                    }
                });
            });
        });
    }

    private static void formatCaseInformation(JsonNode hearing) {
        StringBuilder listingNotes = new StringBuilder();

        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(cases -> {
                if (!cases.has("caseSequenceIndicator")) {
                    ((ObjectNode)cases).put("caseSequenceIndicator", "");
                }
            });
        }

        if (hearing.has("listingDetails")) {
            listingNotes.append(hearing.get("listingDetails").get("listingRepDeadline"));
            listingNotes.append(", ");
        }
        ((ObjectNode)hearing).put("listingNotes", GeneralHelper.trimAnyCharacterFromStringEnd(listingNotes.toString())
            .replace("\"", ""));
    }

    private static void formatCaseHtmlTable(JsonNode hearing) {
        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(cases -> {
                ((ObjectNode)cases).put("bottomBorder", "");
                if (!GeneralHelper.findAndReturnNodeText(hearing, "listingNotes").isBlank()) {
                    ((ObjectNode)cases).put("bottomBorder", "no-border-bottom");
                }
            });
        }
    }

    private static void formatCaseTime(JsonNode sitting) {
        if (!GeneralHelper.findAndReturnNodeText(sitting, "sittingStart").isEmpty()) {
            ((ObjectNode)sitting).put("time",
                                      DateHelper.timeStampToBstTimeWithFormat(GeneralHelper
                    .findAndReturnNodeText(sitting, "sittingStart"), "h:mma"));
        }
    }

    private static void findAndManipulatePartyInformation(JsonNode hearing) {
        StringBuilder prosecutingAuthority = new StringBuilder();
        StringBuilder defendant = new StringBuilder();

        hearing.get("party").forEach(party -> {
            if (!GeneralHelper.findAndReturnNodeText(party, "partyRole").isBlank()) {
                switch (PartyRoleMapper.convertPartyRole(party.get("partyRole").asText())) {
                    case "PROSECUTING_AUTHORITY": {
                        formatPartyInformation(party, prosecutingAuthority);
                        break;
                    }
                    case "DEFENDANT": {
                        formatPartyInformation(party, defendant);
                        break;
                    }
                    default:
                        break;
                }
            }
        });

        ((ObjectNode) hearing).put(PROSECUTING_AUTHORITY,
                                   GeneralHelper.trimAnyCharacterFromStringEnd(prosecutingAuthority.toString()));
        ((ObjectNode) hearing).put(DEFENDANT, GeneralHelper.trimAnyCharacterFromStringEnd(defendant.toString()));
    }

    private static void formatPartyInformation(JsonNode party, StringBuilder builder) {
        String partyDetails = createIndividualDetails(party);
        partyDetails = partyDetails
            + GeneralHelper.stringDelimiter(partyDetails, ", ");
        builder.insert(0, partyDetails);
    }

    private static String createIndividualDetails(JsonNode party) {
        if (party.has("individualDetails")) {
            JsonNode individualDetails = party.get("individualDetails");
            String forNames = GeneralHelper.findAndReturnNodeText(individualDetails, "individualForenames");
            String middleName = GeneralHelper.findAndReturnNodeText(individualDetails, "individualMiddleName");
            String separator = " ";
            if (!forNames.isEmpty() || !middleName.isEmpty()) {
                separator = ", ";
            }
            return (GeneralHelper.findAndReturnNodeText(individualDetails, "title") + " "
                + GeneralHelper.findAndReturnNodeText(individualDetails, "individualSurname")
                + separator
                + forNames + " "
                + middleName).trim();
        }
        return "";
    }
}
