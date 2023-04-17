package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

public final class CopListHelper {
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";
    private static final String TIME_FORMAT = "h:mma";

    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";

    private CopListHelper() {
    }

    public static void manipulateCopListData(JsonNode artefact, Language language) {
        LocationHelper.formatRegionName(artefact);
        LocationHelper.formatRegionalJoh(artefact);

        artefact.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(session -> {
                    ((ObjectNode) session).put(
                        "formattedSessionJoh",
                        DataManipulation.findAndManipulateJudiciaryForCop(session)
                    );
                    session.get(SITTINGS).forEach(sitting -> {
                        DateHelper.calculateDuration(sitting, language);
                        DateHelper.formatStartTime(sitting, TIME_FORMAT, true);
                        DataManipulation.findAndConcatenateHearingPlatform(sitting, session);

                        sitting.get(HEARING).forEach(
                            hearing -> hearing.get("case")
                                .forEach(CopListHelper::manipulateCaseInformation)
                        );
                    });
                })
            )
        );
    }

    private static void manipulateCaseInformation(JsonNode hearingCase) {
        if (!GeneralHelper.findAndReturnNodeText(hearingCase, CASE_SEQUENCE_INDICATOR).isEmpty()) {
            ((ObjectNode) hearingCase).put(
                "caseIndicator",
                hearingCase.get(CASE_SEQUENCE_INDICATOR).asText()
            );
        }
    }
}
