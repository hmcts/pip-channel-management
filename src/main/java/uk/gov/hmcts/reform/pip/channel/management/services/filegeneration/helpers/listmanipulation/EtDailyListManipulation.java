package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.LocationHelper;

import java.util.concurrent.atomic.AtomicReference;

public final class EtDailyListManipulation {

    private static final String CLAIMANT = "claimant";
    private static final String CLAIMANT_REPRESENTATIVE = "claimantRepresentative";
    private static final String RESPONDENT = "respondent";
    private static final String RESPONDENT_REPRESENTATIVE = "respondentRepresentative";

    private EtDailyListManipulation() {
    }

    public static void processRawListData(JsonNode data, Language language) {
        LocationHelper.formatCourtAddress(data, System.lineSeparator());

        data.get("courtLists").forEach(courtList -> {
            courtList.get("courtHouse").get("courtRoom").forEach(courtRoom -> {
                courtRoom.get("session").forEach(session -> {
                    session.get("sittings").forEach(sitting -> {
                        DateHelper.calculateDuration(sitting, language);
                        DateHelper.formatStartTime(sitting,"h:mma", true);
                        DataManipulation.findAndConcatenateHearingPlatform(sitting, session);
                        sitting.get("hearing").forEach(hearing -> {
                            handleParties(hearing);
                        });
                    });
                });
            });
        });
    }

    static void handleParties(JsonNode hearing) {
        AtomicReference<String> claimant = new AtomicReference<>("");
        AtomicReference<String> claimantRepresentative = new AtomicReference<>("");
        AtomicReference<String> respondent = new AtomicReference<>("");
        AtomicReference<String> respondentRepresentative = new AtomicReference<>("");

        hearing.get("party").forEach(party -> {
            if (!GeneralHelper.findAndReturnNodeText(party, "partyRole").isEmpty()) {
                switch (party.get("partyRole").asText()) {
                    case "CLAIMANT_PETITIONER" ->
                        claimant.set(createIndividualDetails(party));
                    case "CLAIMANT_PETITIONER_REPRESENTATIVE" ->
                        claimantRepresentative.set(createIndividualDetails(party));
                    case "RESPONDENT" ->
                        respondent.set(createIndividualDetails(party));
                    case "RESPONDENT_REPRESENTATIVE" ->
                        respondentRepresentative.set(createIndividualDetails(party));
                    default -> { }
                }
            }
        });

        ((ObjectNode) hearing).put(CLAIMANT, claimant.get());
        ((ObjectNode) hearing).put(CLAIMANT_REPRESENTATIVE, claimantRepresentative.get());
        ((ObjectNode) hearing).put(RESPONDENT, respondent.get());
        ((ObjectNode) hearing).put(RESPONDENT_REPRESENTATIVE, respondentRepresentative.get());
    }

    private static String createIndividualDetails(JsonNode party) {
        JsonNode individualDetails = party.get("individualDetails");
        String title = GeneralHelper.findAndReturnNodeText(individualDetails, "title");
        String forenames = GeneralHelper.findAndReturnNodeText(individualDetails, "individualForenames");
        String forenameInitial = forenames.isEmpty() ? "" : forenames.substring(0, 1);
        String surname = GeneralHelper.findAndReturnNodeText(individualDetails, "individualSurname");

        return title + GeneralHelper.stringDelimiter(title, " ")
            + forenameInitial + GeneralHelper.stringDelimiter(forenameInitial, " ")
            + surname;
    }
}
