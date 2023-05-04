package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.Map;

import static uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.MagistratesStandardListHelper.processArtefactForMagistratesStandardListThymeLeaf;

@Service
public class MagistratesStandardListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) {
        return TemplateEngine.processTemplate(
            metadata.get("listType"),
            processArtefactForMagistratesStandardListThymeLeaf(artefact, metadata, language)
        );
    }
}
