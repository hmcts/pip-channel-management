package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.CrownWarnedList;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.CaseHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CrownWarnedListManipulation {
    private static final String DELIMITER = ", ";
    private static final String DEFENDANT = "defendant";
    private static final String DEFENDANT_REPRESENTATIVE = "defendantRepresentative";
    private static final String PROSECUTING_AUTHORITY = "prosecutingAuthority";
    private static final String TO_BE_ALLOCATED = "To be allocated";

    private static final Comparator<Map.Entry<String, List<CrownWarnedList>>> COMPARATOR = (s1, s2) -> {
        if (TO_BE_ALLOCATED.equalsIgnoreCase(s1.getKey())) {
            return 1;
        } else if (TO_BE_ALLOCATED.equalsIgnoreCase(s2.getKey())) {
            return -1;
        }
        return 0;
    };

    private CrownWarnedListManipulation() {
    }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    public static Map<String, List<CrownWarnedList>> processRawListData(JsonNode data, Language language) {
        Map<String, List<CrownWarnedList>> result = new LinkedHashMap<>();
        data.get("courtLists").forEach(courtList -> {
            courtList.get("courtHouse").get("courtRoom").forEach(courtRoom -> {
                courtRoom.get("session").forEach(session -> {
                    session.get("sittings").forEach(sitting -> {
                        String hearingDate = DateHelper.formatTimeStampToBst(sitting.get("sittingStart").asText(),
                                                                             language, false, false,
                                                                             "dd/MM/yyyy");
                        sitting.get("hearing").forEach(hearing -> {
                            String listNote = GeneralHelper.findAndReturnNodeText(hearing, "listNote");
                            handleParties(hearing);
                            List<CrownWarnedList> rows = new ArrayList<>();
                            hearing.get("case").forEach(hearingCase -> {
                                CaseHelper.formatLinkedCases(hearingCase);
                                rows.add(new CrownWarnedList(
                                    hearingCase.get("caseNumber").asText(),
                                    hearing.get(DEFENDANT).asText(),
                                    hearingDate,
                                    hearing.get(DEFENDANT_REPRESENTATIVE).asText(),
                                    hearing.get(PROSECUTING_AUTHORITY).asText(),
                                    hearingCase.get("formattedLinkedCases").asText(),
                                    listNote
                                ));
                            });
                            result.computeIfAbsent(hearing.get("hearingType").asText(), x -> new ArrayList<>())
                                .addAll(rows);
                        });
                    });
                });
            });
        });

        return sort(result);
    }

    static void handleParties(JsonNode hearing) {
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

    private static Map<String, List<CrownWarnedList>> sort(Map<String, List<CrownWarnedList>> cases) {
        return cases.entrySet().stream()
            .sorted(COMPARATOR)
            .collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}
