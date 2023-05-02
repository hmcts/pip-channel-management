package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

public final class EtDailyListManipulation {

    private EtDailyListManipulation() {
    }

    public static void processRawListData(JsonNode data, Language language) {
        LocationHelper.formatCourtAddress(data, System.lineSeparator());

        data.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(sitting -> {
                        DateHelper.calculateDuration(sitting, language);
                        DateHelper.formatStartTime(sitting,"h:mma", true);
                        DataManipulation.findAndConcatenateHearingPlatform(sitting, session);
                        sitting.get("hearing")
                            .forEach(hearing -> PartyRoleHelper.findAndManipulatePartyInformation(hearing, true));
                    })
                )
            )
        );
    }
}
