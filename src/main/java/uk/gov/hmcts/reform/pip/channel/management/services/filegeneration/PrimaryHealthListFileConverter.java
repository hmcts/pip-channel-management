package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.TribunalNationalListHelper;

import java.util.Map;

@Service
public class PrimaryHealthListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources) {
        Context context = TribunalNationalListHelper
            .preprocessArtefactForTribunalNationalListsThymeLeafConverter(artefact, metadata, languageResources);
        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }
}
