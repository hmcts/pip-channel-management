package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;

import java.util.ArrayList;
import java.util.List;

public final class PartyRoleHelper {
    private static final String APPLICANT = "applicant";
    private static final String RESPONDENT = "respondent";
    private static final String CLAIMANT = "claimant";
    private static final String CLAIMANT_REPRESENTATIVE = "claimantRepresentative";
    private static final String PROSECUTING_AUTHORITY = "prosecutingAuthority";
    private static final String DELIMITER = ", ";
    private static final String DEFENDANT = "defendant";
    private static final String DEFENDANT_REPRESENTATIVE = "defendantRepresentative";
    private static final String PARTY_ROLE = "partyRole";

    private PartyRoleHelper() {
    }

    public static void findAndManipulatePartyInformation(JsonNode hearing, Language language, Boolean initialised) {
        StringBuilder applicant = new StringBuilder();
        StringBuilder respondent = new StringBuilder();
        StringBuilder claimant = new StringBuilder();
        StringBuilder claimantRepresentative = new StringBuilder();
        StringBuilder prosecutingAuthority = new StringBuilder();

        hearing.get("party").forEach(party -> {
            if (!GeneralHelper.findAndReturnNodeText(party, PARTY_ROLE).isEmpty()) {
                switch (PartyRoleMapper.convertPartyRole(party.get(PARTY_ROLE).asText())) {
                    case "APPLICANT_PETITIONER" ->
                        formatPartyNonRepresentative(party, applicant, initialised);
                    case "APPLICANT_PETITIONER_REPRESENTATIVE" -> {
                        final String applicantPetitionerDetails = createIndividualDetails(party, initialised);
                        if (!applicantPetitionerDetails.isEmpty()) {
                            applicant
                                .append(language == Language.ENGLISH ? "Legal Advisor: " : "Cynghorydd Cyfreithiol: ")
                                .append(applicantPetitionerDetails)
                                .append(", ");
                        }
                    }
                    case "RESPONDENT" -> {
                        formatPartyNonRepresentative(party, respondent, initialised);
                        formatPartyNonRepresentative(party, prosecutingAuthority, initialised);
                    }
                    case "RESPONDENT_REPRESENTATIVE" ->
                        respondent.append(respondentRepresentative(language, party, initialised));
                    case "CLAIMANT_PETITIONER" ->
                        formatPartyNonRepresentative(party, claimant, initialised);
                    case "CLAIMANT_PETITIONER_REPRESENTATIVE" ->
                        formatPartyNonRepresentative(party, claimantRepresentative, initialised);
                    default -> { }
                }
            }
        });

        ((ObjectNode) hearing).put(APPLICANT, GeneralHelper.trimAnyCharacterFromStringEnd(applicant.toString()));
        ((ObjectNode) hearing).put(RESPONDENT, GeneralHelper.trimAnyCharacterFromStringEnd(respondent.toString()));
        ((ObjectNode) hearing).put(CLAIMANT, GeneralHelper.trimAnyCharacterFromStringEnd(claimant.toString()));
        ((ObjectNode) hearing).put(CLAIMANT_REPRESENTATIVE,
                                   GeneralHelper.trimAnyCharacterFromStringEnd(claimantRepresentative.toString()));
        ((ObjectNode) hearing).put(PROSECUTING_AUTHORITY,
                                   GeneralHelper.trimAnyCharacterFromStringEnd(prosecutingAuthority.toString()));
    }

    private static String respondentRepresentative(Language language, JsonNode respondentDetails,
                                                   Boolean initialised) {
        StringBuilder builder = new StringBuilder();
        final String details = createIndividualDetails(respondentDetails, initialised);
        if (!respondentDetails.isEmpty()) {
            builder
                .append(language == Language.ENGLISH ? "Legal Advisor: " : "Cynghorydd Cyfreithiol: ")
                .append(details)
                .append(", ");
        }
        return builder.toString();
    }

    private static void formatPartyNonRepresentative(JsonNode party, StringBuilder builder, Boolean initialised) {
        String respondentDetails = createIndividualDetails(party, initialised);
        respondentDetails = respondentDetails
            + GeneralHelper.stringDelimiter(respondentDetails, ", ");
        builder.insert(0, respondentDetails);
    }

    private static String createIndividualDetails(JsonNode party, Boolean initialised) {
        if (party.has("individualDetails")) {
            JsonNode individualDetails = party.get("individualDetails");
            if (initialised) {
                String forename = GeneralHelper.findAndReturnNodeText(individualDetails, "individualForenames");
                String forenameInitial = forename.isEmpty() ? "" : forename.substring(0, 1);
                return (GeneralHelper.findAndReturnNodeText(individualDetails, "title") + " "
                    + forenameInitial + " "
                    + GeneralHelper.findAndReturnNodeText(individualDetails, "individualSurname")).trim();
            } else {
                return (GeneralHelper.findAndReturnNodeText(individualDetails, "title") + " "
                    + GeneralHelper.findAndReturnNodeText(individualDetails, "individualForenames") + " "
                    + GeneralHelper.findAndReturnNodeText(individualDetails, "individualMiddleName") + " "
                    + GeneralHelper.findAndReturnNodeText(individualDetails, "individualSurname")).trim();
            }
        }
        return "";
    }

    private static String createIndividualDetails(JsonNode party) {
        JsonNode individualDetails = party.get("individualDetails");
        String forenames = GeneralHelper.findAndReturnNodeText(individualDetails, "individualForenames");
        String surname = GeneralHelper.findAndReturnNodeText(individualDetails, "individualSurname");

        return surname + (surname.isEmpty() || forenames.isEmpty() ? "" : ", ")
            + forenames;
    }

    public static void handleParties(JsonNode hearing) {
        List<String> defendants = new ArrayList<>();
        List<String> defendantRepresentatives = new ArrayList<>();
        List<String> prosecutingAuthorities = new ArrayList<>();

        if (hearing.has("party")) {
            hearing.get("party").forEach(party -> {
                if (!GeneralHelper.findAndReturnNodeText(party, PARTY_ROLE).isEmpty()) {
                    switch (party.get(PARTY_ROLE).asText()) {
                        case "DEFENDANT" ->
                            defendants.add(createIndividualDetails(party));
                        case "DEFENDANT_REPRESENTATIVE" ->
                            defendantRepresentatives.add(createOrganisationDetails(party));
                        case "PROSECUTING_AUTHORITY" ->
                            prosecutingAuthorities.add(createOrganisationDetails(party));
                        default -> { }
                    }
                }
            });
        }

        ((ObjectNode) hearing).put(DEFENDANT, String.join(DELIMITER, defendants));
        ((ObjectNode) hearing).put(DEFENDANT_REPRESENTATIVE, String.join(DELIMITER, defendantRepresentatives));
        ((ObjectNode) hearing).put(PROSECUTING_AUTHORITY, String.join(DELIMITER, prosecutingAuthorities));
    }

    private static String createOrganisationDetails(JsonNode party) {
        JsonNode organisationDetails = party.get("organisationDetails");
        return GeneralHelper.findAndReturnNodeText(organisationDetails, "organisationName");
    }
}
