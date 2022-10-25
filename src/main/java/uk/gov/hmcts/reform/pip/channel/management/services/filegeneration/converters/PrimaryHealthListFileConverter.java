package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import uk.gov.hmcts.reform.pip.channel.management.config.ThymeleafConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation.PrimaryHealthListManipulation;

import java.util.Map;

@Service
public class PrimaryHealthListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources) {
        Context context = new Context();
        Language language = Language.valueOf(metadata.get("language"));
        String publicationDate = artefact.get("document").get("publicationDate").asText();
        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               false, false));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               true, false));
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("email", artefact.get("venue").get("venueContact").get("venueEmail").asText());
        context.setVariable("i18n", languageResources);

        LocationHelper.formatCourtAddress(artefact, "\n", true);
        context.setVariable("cases", PrimaryHealthListManipulation.processRawListData(artefact, language));

        SpringTemplateEngine templateEngine = new ThymeleafConfiguration().templateEngine();
        return templateEngine.process("primaryHealthList.html", context);
    }
}
