package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.MagistratesPublicListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class MagistratesPublicListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        if (GeneralHelper.hearingHasParty(payload)) {
            MagistratesPublicListHelper.manipulatedMagistratesPublicListDataV1(payload, Language.ENGLISH);
            return processMagistratesPublicListV1(payload);
        }
        MagistratesPublicListHelper.manipulatedMagistratesPublicListData(payload, Language.ENGLISH);
        return processMagistratesPublicList(payload);
    }

    public Map<String, List<Map<String, String>>> processMagistratesPublicList(JsonNode node) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        node.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(
                            hearing -> hearing.get("case").forEach(hearingCase -> {
                                Map<String, String> fields = ImmutableMap.of(
                                    "Defendant",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "defendant"),
                                    "Prosecutor",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "prosecutingAuthority"),
                                    "Case reference",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "caseNumber"),
                                    "Hearing type",
                                    GeneralHelper.findAndReturnNodeText(hearing, "hearingType")
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
    public Map<String, List<Map<String, String>>> processMagistratesPublicListV1(JsonNode node) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        node.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(
                            hearing -> hearing.get("case").forEach(hearingCase -> {
                                Map<String, String> fields = ImmutableMap.of(
                                    "Defendant",
                                    GeneralHelper.findAndReturnNodeText(hearing, "defendant"),
                                    "Prosecutor",
                                    GeneralHelper.findAndReturnNodeText(hearing, "prosecutingAuthority"),
                                    "Case reference",
                                    GeneralHelper.findAndReturnNodeText(hearingCase, "caseNumber"),
                                    "Hearing type",
                                    GeneralHelper.findAndReturnNodeText(hearing, "hearingType")
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
}
