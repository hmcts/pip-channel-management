package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.JudiciaryHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.SittingHelper;
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
        ObjectNode artefactObj = (ObjectNode) artefact;
        LocationHelper.formatRegionName(artefactObj);
        LocationHelper.formatRegionalJoh(artefactObj);

        artefact.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(session -> {
                    ((ObjectNode) session).put(
                        "formattedSessionJoh",
                        JudiciaryHelper.findAndManipulateJudiciary(session, false)
                    );
                    session.get(SITTINGS).forEach(sitting -> {
                        DateHelper.calculateDuration(sitting, language);
                        DateHelper.formatStartTime(sitting, TIME_FORMAT, true);
                        SittingHelper.findAndConcatenateHearingPlatform(sitting, session);

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
