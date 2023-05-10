package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.CommonListHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.MagistratesStandardListHelper;

import java.util.Map;

@Service
public class MagistratesStandardListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) {
        return TemplateEngine.processTemplate(
            metadata.get("listType"),
            preprocessArtefactForThymeLeafConverter(artefact, metadata, language)
        );
    }

    private Context preprocessArtefactForThymeLeafConverter(
        JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) {
        Context context = CommonListHelper.preprocessArtefactForThymeLeafConverter(
            artefact, metadata, language, false
        );
        MagistratesStandardListHelper.manipulatedMagistratesStandardList(artefact, language);
        context.setVariable("venueAddress", LocationHelper.formatFullVenueAddress(artefact));
        context.setVariable("version", artefact.get("document").get("version").asText());
        return context;
    }
}
