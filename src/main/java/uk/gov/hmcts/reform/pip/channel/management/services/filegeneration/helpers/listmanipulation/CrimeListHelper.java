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

import java.util.ArrayList;
import java.util.List;
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
    public static final String PROSECUTING_AUTHORITY = "prosecutingAuthority";
    public static final String DEFENDANT = "defendant";
    public static final String DEFENDANT_REPRESENTATIVE = "defendantRepresentative";

    private static final String DELIMITER = ", ";
    private static final String COURT_LIST = "courtLists";
    private static final String CASE = "case";
    private static final String COURT_ROOM = "courtRoom";
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";
    private static final String LISTING_DETAILS = "listingDetails";
    private static final String LISTING_NOTES = "listingNotes";
    private static final String LINKED_CASES = "linkedCases";
    private static final String COURT_ROOM_NAME = "courtRoomName";

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
                    if (GeneralHelper.findAndReturnNodeText(courtRoom, COURT_ROOM_NAME)
                        .contains("to be allocated")) {
                        ((ObjectNode)session).put("formattedSessionCourtRoom",
                                                  GeneralHelper.findAndReturnNodeText(courtRoom, COURT_ROOM_NAME));
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
                    LINKED_CASES,
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
                if (!GeneralHelper.findAndReturnNodeText(cases, LINKED_CASES).isEmpty()
                    || !GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES).isEmpty()) {
                    ((ObjectNode)cases).put("caseCellBorder", "no-border-bottom");
                }

                ((ObjectNode)cases).put("linkedCasesBorder", "");
                if (!GeneralHelper.findAndReturnNodeText(cases, LINKED_CASES).isEmpty()) {
                    ((ObjectNode)cases).put("linkedCasesBorder", "no-border-bottom");
                }
            });
        }
    }

    private static void formatCaseTime(JsonNode sitting) {
        if (!GeneralHelper.findAndReturnNodeText(sitting, "sittingStart").isEmpty()) {
            ((ObjectNode)sitting).put("time",
                                      DateHelper.timeStampToBstTime(
                                          GeneralHelper.findAndReturnNodeText(sitting, "sittingStart"),
                                          "h:mma"));
        }
    }

    public static void findAndManipulatePartyInformation(JsonNode hearing) {
        List<String> defendants = new ArrayList<>();
        List<String> defendantRepresentatives = new ArrayList<>();
        List<String> prosecutingAuthorities = new ArrayList<>();

        hearing.get("party").forEach(party -> {
            if (!GeneralHelper.findAndReturnNodeText(party, "partyRole").isEmpty()) {
                switch (party.get("partyRole").asText()) {
                    case "DEFENDANT":
                        defendants.add(createIndividualDetails(party));
                        break;
                    case "DEFENDANT_REPRESENTATIVE":
                        defendantRepresentatives.add(createOrganisationDetails(party));
                        break;
                    case "PROSECUTING_AUTHORITY":
                        prosecutingAuthorities.add(createOrganisationDetails(party));
                        break;
                    default:
                        break;
                }
            }
        });

        ((ObjectNode) hearing).put(DEFENDANT, String.join(DELIMITER, defendants));
        ((ObjectNode) hearing).put(DEFENDANT_REPRESENTATIVE, String.join(DELIMITER, defendantRepresentatives));
        ((ObjectNode) hearing).put(PROSECUTING_AUTHORITY, String.join(DELIMITER, prosecutingAuthorities));
    }

    private static String createIndividualDetails(JsonNode party) {
        JsonNode individualDetails = party.get("individualDetails");
        String forenames = GeneralHelper.findAndReturnNodeText(individualDetails, "individualForenames");
        String surname = GeneralHelper.findAndReturnNodeText(individualDetails, "individualSurname");

        return surname + (surname.isEmpty() || forenames.isEmpty() ? "" : ", ")
            + forenames;
    }

    private static String createOrganisationDetails(JsonNode party) {
        JsonNode organisationDetails = party.get("organisationDetails");
        return GeneralHelper.findAndReturnNodeText(organisationDetails, "organisationName");
    }
}
