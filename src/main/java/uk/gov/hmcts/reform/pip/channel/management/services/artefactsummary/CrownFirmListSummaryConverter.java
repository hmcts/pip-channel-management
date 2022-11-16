package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation.CrownFirmListHelper;

@Service
public class CrownFirmListSummaryConverter  implements ArtefactSummaryConverter {

    @Override
    public String convert(String payload) throws JsonProcessingException {
        JsonNode node = new ObjectMapper().readTree(payload);
        DataManipulation.manipulatedDailyListData(node, Language.ENGLISH, true);
        CrownFirmListHelper.crownFirmListFormatted(node);
        return this.processCrownFirmList(node);
    }

    private String processCrownFirmList(JsonNode node) {
        return node.get("test").asText();
    }
}
