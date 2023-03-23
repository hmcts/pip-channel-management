package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.channel.management.config.ThymeleafConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation.EtDailyListManipulation;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.Map;

@Service
public class EtDailyListFileConverter implements FileConverter {
    private static final String VENUE_CONTACT = "venueContact";

    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata, Map<String, Object> languageResources) {
        Context context = new Context();
        Language language = Language.valueOf(metadata.get("language"));
        setPublicationDateTime(context, artefact.get("document").get("publicationDate").asText(), language);

        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("region", metadata.get("region"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("i18n", languageResources);

        EtDailyListManipulation.processRawListData(artefact, language);
        context.setVariable("artefact", artefact);
        setVenue(context, artefact.get("venue"));

        return new ThymeleafConfiguration().templateEngine().process("etDailyList.html", context);
    }

    private void setPublicationDateTime(Context context, String publicationDate, Language language) {
        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               false, false));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               true, false));
    }

    private void setVenue(Context context, JsonNode venue) {
        context.setVariable("venueName", GeneralHelper.findAndReturnNodeText(venue, "venueName"));

        String venueEmail = "";
        String venueTelephone = "";
        if (venue.has(VENUE_CONTACT)) {
            venueEmail = GeneralHelper.findAndReturnNodeText(venue.get(VENUE_CONTACT),"venueEmail");
            venueTelephone = GeneralHelper.findAndReturnNodeText(venue.get(VENUE_CONTACT),"venueTelephone");
        }

        context.setVariable("venueEmail", venueEmail);
        context.setVariable("venueTelephone", venueTelephone);
    }

}
