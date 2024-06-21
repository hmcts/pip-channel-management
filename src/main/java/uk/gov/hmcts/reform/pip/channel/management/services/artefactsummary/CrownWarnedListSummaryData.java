package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.CrownWarnedList;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.CrownWarnedListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class CrownWarnedListSummaryData implements ArtefactSummaryData {
    @Override
    public Map<String, List<Map<String, String>>> get(JsonNode payload) throws JsonProcessingException {
        Map<String, List<CrownWarnedList>> cases = GeneralHelper.hearingHasParty(payload)
            ? CrownWarnedListHelper.processRawListDataV1(payload, Language.ENGLISH)
            : CrownWarnedListHelper.processRawListData(payload, Language.ENGLISH);
        List<Map<String, String>> summaryCases = new ArrayList<>();

        cases.values()
            .stream()
            .flatMap(Collection::stream)
            .forEach(row -> {
                Map<String, String> fields = ImmutableMap.of(
                    "Defendant", row.getDefendant(),
                    "Prosecutor", row.getProsecutingAuthority(),
                    "Case reference", row.getCaseReference(),
                    "Hearing date", row.getHearingDate()
                );
                summaryCases.add(fields);
            });
        return Collections.singletonMap(null, summaryCases);
    }
}
