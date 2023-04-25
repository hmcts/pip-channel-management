package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.PartyRoleMapper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

@SuppressWarnings("java:S108")
public final class FamilyMixedListHelper {
    private static final String APPLICANT = "applicant";
    private static final String APPLICANT_REPRESENTATIVE = "applicantRepresentative";
    private static final String RESPONDENT = "respondent";
    private static final String RESPONDENT_REPRESENTATIVE = "respondentRepresentative";
    private static final String PARTY_ROLE = "partyRole";

    private FamilyMixedListHelper() {
    }

    public static void manipulatedlistData(JsonNode artefact, Language language) {
        artefact.get("courtLists")
            .forEach(courtList -> courtList.get(LocationHelper.COURT_HOUSE).get("courtRoom")
                .forEach(courtRoom -> courtRoom.get("session").forEach(session -> {
                    StringBuilder formattedJudiciary = new StringBuilder();
                    formattedJudiciary.append(DataManipulation.findAndManipulateJudiciary(session));
                    session.get("sittings").forEach(sitting -> {
                        DateHelper.calculateDuration(sitting, language);
                        DateHelper.formatStartTime(sitting, "h:mma", true);
                        DataManipulation.findAndConcatenateHearingPlatform(sitting, session);

                        sitting.get("hearing").forEach(hearing -> {
                            if (hearing.has("party")) {
                                handleParties(hearing);
                            } else {
                                ((ObjectNode) hearing).put(APPLICANT, "");
                                ((ObjectNode) hearing).put(RESPONDENT, "");
                            }
                            hearing.get("case").forEach(DataManipulation::manipulateCaseInformation);
                        });
                    });
                    LocationHelper.formattedCourtRoomName(courtRoom, session, formattedJudiciary);
                })));
    }

    private static void handleParties(JsonNode hearing) {
        StringBuilder applicant = new StringBuilder();
        StringBuilder applicantRepresentative = new StringBuilder();
        StringBuilder respondent = new StringBuilder();
        StringBuilder respondentRepresentative = new StringBuilder();

        hearing.get("party").forEach(party -> {
            if (!GeneralHelper.findAndReturnNodeText(party, PARTY_ROLE).isEmpty()) {
                switch (PartyRoleMapper.convertPartyRole(party.get(PARTY_ROLE).asText())) {
                    case "APPLICANT_PETITIONER" ->
                        PartyRoleHelper.formatPartyDetails(applicant, createPartyDetails(party));
                    case "APPLICANT_PETITIONER_REPRESENTATIVE" ->
                        PartyRoleHelper.formatPartyDetails(applicantRepresentative,
                                                           createPartyDetails(party));
                    case "RESPONDENT" ->
                        PartyRoleHelper.formatPartyDetails(respondent, createPartyDetails(party));
                    case "RESPONDENT_REPRESENTATIVE" ->
                        PartyRoleHelper.formatPartyDetails(respondentRepresentative,
                                                           createPartyDetails(party));
                    default -> { }
                }
            }
        });

        ((ObjectNode) hearing).put(APPLICANT,
                                   GeneralHelper.trimAnyCharacterFromStringEnd(applicant.toString()));
        ((ObjectNode) hearing).put(APPLICANT_REPRESENTATIVE,
                                   GeneralHelper.trimAnyCharacterFromStringEnd(applicantRepresentative.toString()));
        ((ObjectNode) hearing).put(RESPONDENT,
                                   GeneralHelper.trimAnyCharacterFromStringEnd(respondent.toString()));
        ((ObjectNode) hearing).put(RESPONDENT_REPRESENTATIVE,
                                   GeneralHelper.trimAnyCharacterFromStringEnd(respondentRepresentative.toString()));
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
