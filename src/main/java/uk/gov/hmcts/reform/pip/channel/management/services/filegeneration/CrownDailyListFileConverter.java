package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring5.SpringTemplateEngine;
import uk.gov.hmcts.reform.pip.channel.management.config.ThymeleafConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.CrownDailyListHelper;

import java.util.Map;

@Service
public class CrownDailyListFileConverter implements FileConverter {
    @Override
    public String convert(JsonNode artefact, Map<String, String> artefactValues, Map<String, Object> language) {
        SpringTemplateEngine templateEngine = new ThymeleafConfiguration().templateEngine();
        return templateEngine.process("crownDailyList.html",
                                      CrownDailyListHelper.preprocessArtefactForThymeLeafConverter(
                                          artefact, artefactValues, language));
    }
}
