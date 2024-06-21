package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.CrownFirmListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class CrownFirmListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) throws JsonProcessingException {
        if (GeneralHelper.hearingHasParty(payload)) {
            CrownFirmListHelper.crownFirmListFormattedV1(payload, Language.ENGLISH);
            CrownFirmListHelper.splitByCourtAndDateV1(payload);
            return processCrownFirmListV1(payload);
        }
        CrownFirmListHelper.crownFirmListFormatted(payload, Language.ENGLISH);
        CrownFirmListHelper.splitByCourtAndDate(payload);
        return processCrownFirmList(payload);
    }

    private Map<String, List<Map<String, String>>> processCrownFirmList(JsonNode node) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        node.get("courtListsByDate").forEach(
            courtLists -> courtLists.forEach(
                courtList -> courtList.get("courtRooms").forEach(
                    courtRoom -> courtRoom.get("hearings").forEach(
                        hearings -> hearings.forEach(
                            hearing -> hearing.get("case").forEach(hearingCase -> {
                                Map<String, String> fields = ImmutableMap.of(
                                    "Defendant",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "defendant"),
                                    "Prosecutor",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "prosecutingAuthority"),
                                    "Case reference",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "caseReference"),
                                    "Hearing type",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "hearingType")
                                );
                                summaryCases.add(fields);
                            })
                        )
                    )
                )
            )
        );
        return Collections.singletonMap(null, summaryCases);
    }

    @Deprecated
    private Map<String, List<Map<String, String>>> processCrownFirmListV1(JsonNode node) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        node.get("courtListsByDate").forEach(
            courtLists -> courtLists.forEach(
                courtList -> courtList.get("courtRooms").forEach(
                    courtRoom -> courtRoom.get("hearings").forEach(
                        hearings -> hearings.forEach(hearing -> {
                            Map<String, String> fields = ImmutableMap.of(
                                "Defendant",
                                GeneralHelper.findAndReturnNodeText(hearing, "defendant"),
                                "Prosecutor",
                                GeneralHelper.findAndReturnNodeText(hearing, "prosecutingAuthority"),
                                "Case reference",
                                GeneralHelper.findAndReturnNodeText(hearing, "caseReference"),
                                "Hearing type",
                                GeneralHelper.findAndReturnNodeText(hearing, "hearingType")
                            );
                            summaryCases.add(fields);
                        })
                    )
                )
            )
        );
        return Collections.singletonMap(null, summaryCases);
    }
}
