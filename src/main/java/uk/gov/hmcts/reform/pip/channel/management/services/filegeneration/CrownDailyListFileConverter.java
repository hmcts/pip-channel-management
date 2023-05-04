package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.CrownDailyListHelper;

import java.util.Map;

@Service
public class CrownDailyListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) {
        return TemplateEngine.processTemplate(
            metadata.get("listType"),
            CrownDailyListHelper.preprocessArtefactForThymeLeafConverter(artefact, metadata, language)
        );
    }
}
