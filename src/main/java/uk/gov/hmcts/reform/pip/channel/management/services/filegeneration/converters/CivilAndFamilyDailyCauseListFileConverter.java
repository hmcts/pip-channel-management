package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring5.SpringTemplateEngine;
import uk.gov.hmcts.reform.pip.channel.management.config.ThymeleafConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.LanguageResourceHelper;

import java.io.IOException;
import java.util.Map;

import static uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DailyCauseListHelper.preprocessArtefactForThymeLeafConverter;

@Service
public class CivilAndFamilyDailyCauseListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> artefactValues, Map<String, Object> languageResources)
        throws IOException {
        Language language = Language.valueOf(artefactValues.get("language"));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath("openJusticeStatement", language));

        SpringTemplateEngine templateEngine = new ThymeleafConfiguration().templateEngine();
        return templateEngine.process(
            "civilAndFamilyDailyCauseList.html",
            preprocessArtefactForThymeLeafConverter(artefact, artefactValues, languageResources, false)
        );
    }
}
