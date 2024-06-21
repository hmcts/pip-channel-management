package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.oparesults.Offence;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.OpaResultsHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OpaResultsSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        OpaResultsHelper.processRawListData(payload).forEach(
            (pleaData, list) -> list.forEach(item -> {
                String offenceTitles = item.getOffences()
                    .stream()
                    .map(Offence::getOffenceTitle)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(", "));

                Map<String, String> fields = ImmutableMap.of(
                    "Defendant", item.getDefendant(),
                    "Case reference", item.getCaseUrn(),
                    "Offence", offenceTitles
                );
                summaryCases.add(fields);
            })
        );

        return Collections.singletonMap(null, summaryCases);
    }
}
