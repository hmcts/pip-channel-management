package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import uk.gov.hmcts.reform.pip.channel.management.config.ThymeleafConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.CourtHouse;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SscsDailyListFileConverter implements FileConverter {

    @Override
    public String convert(JsonNode highestLevelNode, Map<String, String> metadata, Map<String, Object> language)
        throws IOException {
        Context context = new Context();
        context.setVariable("i18n", language);
        context.setVariable("metadata", metadata);
        context.setVariable("telephone", GeneralHelper.safeGet("venue.venueContact.venueTelephone", highestLevelNode));
        context.setVariable("email", GeneralHelper.safeGet("venue.venueContact.venueEmail", highestLevelNode));
        context.setVariable("publishedDate", DateHelper.formatTimestampToBst(
            GeneralHelper.safeGet("document.publicationDate", highestLevelNode),
            Language.valueOf(metadata.get("language"))));

        List<CourtHouse> listOfCourtHouses = new ArrayList<>();
        for (JsonNode courtHouse : highestLevelNode.get("courtLists")) {
            listOfCourtHouses.add(DataManipulation.courtHouseBuilder(courtHouse));
        }
        context.setVariable("courtList", listOfCourtHouses);
        SpringTemplateEngine templateEngine = new ThymeleafConfiguration().templateEngine();
        return templateEngine.process("sscsDailyList.html", context);
    }
}


