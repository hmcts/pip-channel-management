package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.MagistratesPublicListHelper;

import java.util.Map;

@Service
public class MagistratesPublicListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) {
        return TemplateEngine.processTemplate(
            metadata.get("listType"),
            MagistratesPublicListHelper.preprocessArtefactForThymeLeafConverter(artefact, metadata, language)
        );
    }
}
