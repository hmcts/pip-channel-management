package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;

import java.util.Map;

public final class DailyCauseListHelper {
    private DailyCauseListHelper() {
    }

    public static Context preprocessArtefactForThymeLeafConverter(JsonNode artefact, Map<String, String> metadata,
                                                                  Map<String, Object> language, Boolean initialised) {
        Context context = new Context();
        LocationHelper.formatCourtAddress(artefact, "|");
        context.setVariable("metadata", metadata);
        context.setVariable("i18n", language);
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
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("venueAddress", LocationHelper.formatFullVenueAddress(artefact));
        context.setVariable("artefact", artefact);
        context.setVariable("phone", artefact.get("venue").get("venueContact").get("venueTelephone").asText());
        context.setVariable("email", artefact.get("venue").get("venueContact").get("venueEmail").asText());
        context.setVariable("version", artefact.get("document").get("version").asText());

        DataManipulation.manipulatedDailyListData(artefact, Language.valueOf(metadata.get("language")), initialised);
        return context;
    }
}
