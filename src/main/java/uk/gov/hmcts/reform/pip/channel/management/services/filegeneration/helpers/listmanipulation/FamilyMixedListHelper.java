package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.PartyRoleMapper;

public final class FamilyMixedListHelper {
    private static final String APPLICANT = "applicant";
    private static final String RESPONDENT = "respondent";
    private static final String PARTY_ROLE = "partyRole";

    private FamilyMixedListHelper() {
    }

    public static void manipulatedlistData(JsonNode artefact, Language language) {
        artefact.get("courtLists").forEach(courtList -> {
            courtList.get(LocationHelper.COURT_HOUSE).get("courtRoom").forEach(courtRoom -> {
                courtRoom.get("session").forEach(session -> {
                    StringBuilder formattedJudiciary = new StringBuilder();
                    formattedJudiciary.append(DataManipulation.findAndManipulateJudiciary(session));
                    session.get("sittings").forEach(sitting -> {
                        DateHelper.calculateDuration(sitting, language);
                        DateHelper.formatStartTime(sitting, "h:mma", true);
                        DataManipulation.findAndConcatenateHearingPlatform(sitting, session);

                        sitting.get("hearing").forEach(hearing -> {
                            if (hearing.has("party")) {
                                handleParties(hearing, language);
                            } else {
                                ((ObjectNode) hearing).put(APPLICANT, "");
                                ((ObjectNode) hearing).put(RESPONDENT, "");
                            }
                            hearing.get("case").forEach(DataManipulation::manipulateCaseInformation);
                        });
                    });
                    LocationHelper.formattedCourtRoomName(courtRoom, session, formattedJudiciary);
                });
            });
        });
    }

    private static void handleParties(JsonNode hearing, Language language) {
        StringBuilder applicant = new StringBuilder();
        StringBuilder respondent = new StringBuilder();

        hearing.get("party").forEach(party -> {
            if (!GeneralHelper.findAndReturnNodeText(party, PARTY_ROLE).isEmpty()) {
                switch (PartyRoleMapper.convertPartyRole(party.get(PARTY_ROLE).asText())) {
                    case "APPLICANT_PETITIONER" ->
                        PartyRoleHelper.formatPartyNonRepresentative(applicant, createPartyDetails(party));
                    case "APPLICANT_PETITIONER_REPRESENTATIVE" ->
                        applicant.append(
                            PartyRoleHelper.formatPartyRepresentative(language, party, createPartyDetails(party))
                        );
                    case "RESPONDENT" ->
                        PartyRoleHelper.formatPartyNonRepresentative(respondent, createPartyDetails(party));
                    case "RESPONDENT_REPRESENTATIVE" ->
                        respondent.append(
                            PartyRoleHelper.formatPartyRepresentative(language, party, createPartyDetails(party))
                        );
                    default -> { }
                }
            }
        });

        ((ObjectNode) hearing).put(APPLICANT, GeneralHelper.trimAnyCharacterFromStringEnd(applicant.toString()));
        ((ObjectNode) hearing).put(RESPONDENT, GeneralHelper.trimAnyCharacterFromStringEnd(respondent.toString()));
    }

    private static String createPartyDetails(JsonNode party) {
        if (party.has("individualDetails")) {
            return PartyRoleHelper.createIndividualDetails(party, false);
        } else if (party.has("organisationDetails")) {
            return GeneralHelper.findAndReturnNodeText(party.get("organisationDetails"), "organisationName");
        }
        return "";
    }
}
