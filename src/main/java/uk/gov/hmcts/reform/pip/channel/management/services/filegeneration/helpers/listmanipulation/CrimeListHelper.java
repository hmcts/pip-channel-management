package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.ListType;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.PartyRoleMapper;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DailyCauseListHelper.preprocessArtefactForThymeLeafConverter;

/**
 * Helper class for crime lists.
 *  Crown Daily List.
 *  Magistrates Public List.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.LawOfDemeter"})
public final class CrimeListHelper {

    public static final String PROSECUTING_AUTHORITY = "prosecuting_authority";
    public static final String DEFENDANT = "defendant";
    public static final String COURT_LIST = "courtLists";
    public static final String CASE = "case";
    public static final String COURT_ROOM = "courtRoom";
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";
    private static final String LISTING_DETAILS = "listingDetails";
    private static final String LISTING_NOTES = "listingNotes";

    private CrimeListHelper() {
    }

    public static Context preprocessArtefactForCrimeListsThymeLeafConverter(
        JsonNode artefact, Map<String, String> metadata, Map<String, Object> language, ListType listType) {
        Context context;
        context = preprocessArtefactForThymeLeafConverter(artefact, metadata, language, false);

        if (ListType.CROWN_DAILY_LIST.equals(listType)) {
            findUnallocatedCasesInCrownDailyListData(artefact);
        }

        manipulatedCrimeListData(artefact, listType);
        formattedCourtRoomName(artefact);
        context.setVariable("version", artefact.get("document").get("version").asText());
        return context;
    }

    public static void manipulatedCrimeListData(JsonNode artefact, ListType listType) {
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

                            if (ListType.CROWN_DAILY_LIST.equals(listType)) {
                                formatCaseInformationCrownDaily(hearing);
                                formatCaseHtmlTableCrownDailyList(hearing);
                            } else if (ListType.MAGISTRATES_PUBLIC_LIST.equals(listType)) {
                                formatCaseInformationMagistratesPublic(hearing);
                                formatCaseHtmlTableMagistratesPublic(hearing);
                            }
                        });
                    });
                });
            });
        });
    }

    public static void findUnallocatedCasesInCrownDailyListData(JsonNode artefact) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode unAllocatedCasesNodeArray = mapper.createArrayNode();
        artefact.get(COURT_LIST).forEach(courtList -> {
            final int[] roomCount = {0};
            courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM).forEach(courtRoom -> {
                if (GeneralHelper.findAndReturnNodeText(courtRoom, "courtRoomName").contains("to be allocated")) {
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
            ((ObjectNode)cloneCourtList.get(LocationHelper.COURT_HOUSE)).put("courtHouseName", "");
            ((ObjectNode)cloneCourtList.get(LocationHelper.COURT_HOUSE)).put("courtHouseAddress", "");
            ((ObjectNode)cloneCourtList).put("unallocatedCases", true);
            ((ObjectNode)cloneCourtList.get(LocationHelper.COURT_HOUSE))
                .putArray(COURT_ROOM).addAll(unAllocatedCasesNodeArray);

            ArrayNode courtListArray = mapper.createArrayNode();

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

    private static void formatCaseInformationMagistratesPublic(JsonNode hearing) {
        StringBuilder listingNotes = new StringBuilder();

        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(cases -> {
                if (!cases.has(CASE_SEQUENCE_INDICATOR)) {
                    ((ObjectNode)cases).put(CASE_SEQUENCE_INDICATOR, "");
                }
            });
        }

        if (hearing.has(LISTING_DETAILS)) {
            listingNotes.append(hearing.get(LISTING_DETAILS).get("listingRepDeadline"));
            listingNotes.append(", ");
        }
        ((ObjectNode)hearing).put(LISTING_NOTES, GeneralHelper.trimAnyCharacterFromStringEnd(listingNotes.toString())
            .replace("\"", ""));
    }



    private static void formatCaseInformationCrownDaily(JsonNode hearing) {
        AtomicReference<StringBuilder> linkedCases = new AtomicReference<>(new StringBuilder());
        StringBuilder listingNotes = new StringBuilder();

        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(cases -> {
                linkedCases.set(new StringBuilder());
                if (cases.has("caseLinked")) {
                    cases.get("caseLinked").forEach(caseLinked -> {
                        linkedCases.get()
                            .append(GeneralHelper.findAndReturnNodeText(caseLinked, "caseId")).append(", ");
                    });
                }
                ((ObjectNode) cases).put(
                    "linkedCases",
                    GeneralHelper.trimAnyCharacterFromStringEnd(linkedCases.toString())
                );

                if (!cases.has(CASE_SEQUENCE_INDICATOR)) {
                    ((ObjectNode) cases).put(CASE_SEQUENCE_INDICATOR, "");
                }
            });
        }

        if (hearing.has(LISTING_DETAILS)) {
            listingNotes.append(hearing.get(LISTING_DETAILS).get("listingRepDeadline"));
            listingNotes.append(", ");
        }
        ((ObjectNode) hearing).put(LISTING_NOTES,
                                   GeneralHelper.trimAnyCharacterFromStringEnd(listingNotes.toString())
                                       .replace("\"", "")
        );
    }

    private static void formatCaseHtmlTableMagistratesPublic(JsonNode hearing) {
        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(cases -> {
                ((ObjectNode)cases).put("bottomBorder", "");
                if (!GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES).isBlank()) {
                    ((ObjectNode)cases).put("bottomBorder", "no-border-bottom");
                }
            });
        }
    }

    private static void formatCaseHtmlTableCrownDailyList(JsonNode hearing) {
        if (hearing.has(CASE)) {
            hearing.get(CASE).forEach(cases -> {
                ((ObjectNode)cases).put("caseCellBorder", "");
                if (!GeneralHelper.findAndReturnNodeText(cases, "linkedCases").isEmpty()
                    || !GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES).isEmpty()) {
                    ((ObjectNode)cases).put("caseCellBorder", "no-border-bottom");
                }

                ((ObjectNode)cases).put("linkedCasesBorder", "");
                if (!GeneralHelper.findAndReturnNodeText(cases, "linkedCases").isEmpty()) {
                    ((ObjectNode)cases).put("linkedCasesBorder", "no-border-bottom");
                }
            });
        }
    }

    private static void formatCaseTime(JsonNode sitting) {
        if (!GeneralHelper.findAndReturnNodeText(sitting, "sittingStart").isEmpty()) {
            ((ObjectNode)sitting).put("time",
                                      DateHelper.timeStampToBstTimeWithFormat(
                                          GeneralHelper.findAndReturnNodeText(sitting, "sittingStart"),
                                          "h:mma"));
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
        ((ObjectNode) hearing).put(DEFENDANT,
                                   GeneralHelper.trimAnyCharacterFromStringEnd(defendant.toString()));
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
