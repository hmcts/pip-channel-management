package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.TribunalNationalListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class TribunalNationalListsSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) {
        List<Map<String, String>> summaryCases = new ArrayList<>();
        LocationHelper.formatCourtAddress(payload, ", ", true);

        TribunalNationalListHelper.processRawListData(payload, Language.ENGLISH)
            .forEach(data -> {
                Map<String, String> fields = ImmutableMap.of(
                    "Case Name", data.getCaseName(),
                    "Hearing Date", data.getHearingDate(),
                    "Hearing Type", data.getHearingType()
                );
                summaryCases.add(fields);
            });

        return Collections.singletonMap(null, summaryCases);
    }
}
