package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import uk.gov.hmcts.reform.pip.channel.management.config.ThymeleafConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;

import java.util.Map;

@Service
public class CopDailyCauseListFileConverter implements FileConverter {

    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) {
        Context context = new Context();
        String publicationDate = artefact.get("document").get("publicationDate").asText();

        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate,
                                                                               Language.valueOf(metadata.get(
                                                                                   "language")),
                                                                               false, false));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate,
                                                                               Language.valueOf(metadata.get(
                                                                                   "language")),
                                                                               true, false));
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("locationDetails", artefact.get("locationDetails"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("artefact", artefact);
        context.setVariable("i18n", language);

        DataManipulation.manipulateCopListData(artefact, Language.valueOf(metadata.get("language")));

        SpringTemplateEngine templateEngine = new ThymeleafConfiguration().templateEngine();
        return templateEngine.process("copDailyCauseList.html", context);
    }
}
