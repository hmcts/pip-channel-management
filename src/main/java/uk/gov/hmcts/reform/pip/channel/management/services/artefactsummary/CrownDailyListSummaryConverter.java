package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.helpers.CrimeListSummaryHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation.CrimeListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

@Service
public class CrownDailyListSummaryConverter implements ArtefactSummaryConverter {

    /**
     * Crown daily list parent method - iterates on courtHouse/courtList - if these need to be shown in further
     * iterations, do it here.
     *
     * @param payload - json body.
     * @return - string for output.
     * @throws JsonProcessingException - jackson req.
     */
    @Override
    public String convert(String payload) throws JsonProcessingException {
        JsonNode node = new ObjectMapper().readTree(payload);

        DataManipulation.manipulatedDailyListData(node, Language.ENGLISH, false);
        CrimeListHelper.manipulatedCrimeListData(node, ListType.CROWN_DAILY_LIST);
        CrimeListHelper.findUnallocatedCasesInCrownDailyListData(node);

        return CrimeListSummaryHelper.processCrimeList(node, ListType.CROWN_DAILY_LIST);
    }
}
