package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.CrownWarnedList;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.CaseHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.PartyRoleHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation.CrimeListHelper.DEFENDANT;
import static uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation.CrimeListHelper.DEFENDANT_REPRESENTATIVE;
import static uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation.CrimeListHelper.PROSECUTING_AUTHORITY;

public final class CrownWarnedListManipulation {

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
        data.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(sitting -> {
                        String hearingDate = DateHelper.formatTimeStampToBst(sitting.get("sittingStart").asText(),
                                                                             language, false, false,
                                                                             "dd/MM/yyyy");
                        sitting.get("hearing").forEach(hearing -> {
                            String listNote = GeneralHelper.findAndReturnNodeText(hearing, "listNote");
                            PartyRoleHelper.handleParties(hearing);
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
                    })
                )
            )
        );

        return sort(result);
    }

    private static Map<String, List<CrownWarnedList>> sort(Map<String, List<CrownWarnedList>> cases) {
        return cases.entrySet().stream()
            .sorted(COMPARATOR)
            .collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}
