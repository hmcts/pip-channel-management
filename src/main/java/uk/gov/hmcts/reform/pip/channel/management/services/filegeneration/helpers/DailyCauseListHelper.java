package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation.FamilyMixedListHelper;

import java.util.Map;

import static uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.ListType.FAMILY_DAILY_CAUSE_LIST;

public final class DailyCauseListHelper {
    public static final String DOCUMENT = "document";
    public static final String VERSION = "version";

    private DailyCauseListHelper() {
    }

    public static Context preprocessArtefactForThymeLeafConverter(JsonNode artefact, Map<String, String> metadata,
                                                                  Map<String, Object> languageResources,
                                                                  Boolean initialised) {
        Context context = new Context();
        LocationHelper.formatCourtAddress(artefact, "|");
        context.setVariable("metadata", metadata);
        context.setVariable("i18n", languageResources);

        Language language = Language.valueOf(metadata.get("language"));
        String publicationDate = artefact.get("document").get("publicationDate").asText();
        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               false, false));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               true, false));
        context.setVariable("contentDate", metadata.get("contentDate"));
        context.setVariable("locationName", metadata.get("locationName"));
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("venueAddress", LocationHelper.formatFullVenueAddress(artefact));
        context.setVariable("artefact", artefact);
        if (artefact.get(DOCUMENT).has(VERSION)) {
            context.setVariable(VERSION, artefact.get(DOCUMENT).get(VERSION).asText());
        }

        if (artefact.get("venue").has("venueContact")) {
            context.setVariable("phone", artefact.get("venue").get("venueContact").get("venueTelephone").asText());
            context.setVariable("email", artefact.get("venue").get("venueContact").get("venueEmail").asText());
        } else {
            context.setVariable("phone", "");
            context.setVariable("email", "");
        }

        String listType = metadata.get("listType");
        if (FAMILY_DAILY_CAUSE_LIST.name().equals(listType)
            || CIVIL_AND_FAMILY_DAILY_CAUSE_LIST.name().equals(listType)) {
            FamilyMixedListHelper.manipulatedlistData(artefact, language);
        } else {
            DataManipulation.manipulatedDailyListData(artefact, language, initialised);
        }
        return context;
    }
}
