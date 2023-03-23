package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import uk.gov.hmcts.reform.pip.channel.management.config.ThymeleafConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.util.Map;

@Service
public class CopDailyCauseListFileConverter implements FileConverter {

    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources)
        throws IOException {
        Context context = new Context();
        Language language = Language.valueOf(metadata.get("language"));
        languageResources.putAll(LanguageResourceHelper.readResourcesFromPath("openJusticeStatement", language));

        String publicationDate = artefact.get("document").get("publicationDate").asText();
        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               false, false));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               true, false));

        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("artefact", artefact);
        context.setVariable("i18n", languageResources);

        DataManipulation.manipulateCopListData(artefact, Language.valueOf(metadata.get("language")));

        SpringTemplateEngine templateEngine = new ThymeleafConfiguration().templateEngine();
        return templateEngine.process("copDailyCauseList.html", context);
    }
}
