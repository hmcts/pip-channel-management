package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring5.SpringTemplateEngine;
import uk.gov.hmcts.reform.pip.channel.management.config.ThymeleafConfiguration;

import java.util.Map;

import static uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation.EtFortnightlyPressListHelper.preprocessArtefactForEtFortnightlyListThymeLeafConverter;

@Service
public class EtFortnightlyPressListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> artefactValues, Map<String, Object> language) {
        SpringTemplateEngine templateEngine = new ThymeleafConfiguration().templateEngine();
        return templateEngine.process("etFortnightlyPressList.html",
            preprocessArtefactForEtFortnightlyListThymeLeafConverter(artefact, artefactValues, language));
    }
}
