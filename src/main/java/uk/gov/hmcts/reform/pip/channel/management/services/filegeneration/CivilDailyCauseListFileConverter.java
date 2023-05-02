package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.thymeleaf.spring5.SpringTemplateEngine;
import uk.gov.hmcts.reform.pip.channel.management.config.ThymeleafConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.util.Map;

import static uk.gov.hmcts.reform.pip.channel.management.services.helpers.DailyCauseListHelper.preprocessArtefactForThymeLeafConverter;

public class CivilDailyCauseListFileConverter implements FileConverter {

    @Override
    public String convert(JsonNode artefact, Map<String, String> artefactValues, Map<String, Object> languageResources)
        throws IOException {
        Language language = Language.valueOf(artefactValues.get("language"));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath("openJusticeStatement", language));

        SpringTemplateEngine templateEngine = new ThymeleafConfiguration().templateEngine();
        return templateEngine.process(
            "civilDailyCauseList.html",
            preprocessArtefactForThymeLeafConverter(artefact, artefactValues, languageResources, false)
        );
    }
}
