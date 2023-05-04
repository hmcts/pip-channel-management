package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.TribunalNationalListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.util.Map;

@Service
public class CareStandardsListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources)
        throws IOException {
        Language language = Language.valueOf(metadata.get("language"));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath("openJusticeStatement", language));

        Context context = TribunalNationalListHelper.preprocessArtefactForThymeLeafConverter(
            artefact, metadata, languageResources
        );
        return TemplateEngine.processTemplate(metadata.get("listType"), context);
    }
}
