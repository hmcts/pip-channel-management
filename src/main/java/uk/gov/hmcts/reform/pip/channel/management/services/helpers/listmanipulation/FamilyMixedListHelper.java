package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.CaseHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.JudiciaryHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleMapper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.SittingHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

@SuppressWarnings("java:S108")
public final class FamilyMixedListHelper {
    private static final String APPLICANT = "applicant";
    private static final String APPLICANT_REPRESENTATIVE = "applicantRepresentative";
    private static final String RESPONDENT = "respondent";
    private static final String RESPONDENT_REPRESENTATIVE = "respondentRepresentative";
    private static final String PARTY_ROLE = "partyRole";
    private static final String COURT_HOUSE = "courtHouse";

    private FamilyMixedListHelper() {
    }

    public static void manipulatedlistData(JsonNode artefact, Language language) {
        artefact.get("courtLists")
            .forEach(courtList -> courtList.get(COURT_HOUSE).get("courtRoom")
                .forEach(courtRoom -> courtRoom.get("session").forEach(session -> {
                    StringBuilder formattedJudiciary = new StringBuilder();
                    formattedJudiciary.append(JudiciaryHelper.findAndManipulateJudiciary(session, true));
                    session.get("sittings").forEach(sitting -> {
                        DateHelper.calculateDuration(sitting, language);
                        DateHelper.formatStartTime(sitting, "h:mma", true);
                        SittingHelper.findAndConcatenateHearingPlatform(sitting, session);

                        sitting.get("hearing").forEach(hearing -> {
                            if (hearing.has("party")) {
                                handleParties(hearing);
                            } else {
                                ObjectNode hearingObj = (ObjectNode) hearing;
                                hearingObj.put(APPLICANT, "");
                                hearingObj.put(RESPONDENT, "");
                            }
                            hearing.get("case").forEach(
                                hearingCase -> CaseHelper.manipulateCaseInformation((ObjectNode) hearingCase)
                            );
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

        ObjectNode hearingObj = (ObjectNode) hearing;
        hearingObj.put(APPLICANT,
                       GeneralHelper.trimAnyCharacterFromStringEnd(applicant.toString()));
        hearingObj.put(APPLICANT_REPRESENTATIVE,
                       GeneralHelper.trimAnyCharacterFromStringEnd(applicantRepresentative.toString()));
        hearingObj.put(RESPONDENT,
                       GeneralHelper.trimAnyCharacterFromStringEnd(respondent.toString()));
        hearingObj.put(RESPONDENT_REPRESENTATIVE,
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
