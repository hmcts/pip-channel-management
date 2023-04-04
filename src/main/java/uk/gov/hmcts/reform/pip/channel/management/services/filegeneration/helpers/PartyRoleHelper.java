package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.poi.util.StringUtil;
import uk.gov.hmcts.reform.pip.model.publication.Language;

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

    private static final String PARTY = "party";
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String INDIVIDUAL_FORENAMES = "individualForenames";
    private static final String INDIVIDUAL_MIDDLE_NAME = "individualMiddleName";
    private static final String INDIVIDUAL_SURNAME = "individualSurname";
    private static final String TITLE = "title";

    private PartyRoleHelper() {
    }

    public static void findAndManipulatePartyInformation(JsonNode hearing, Language language, boolean initialised) {
        StringBuilder applicant = new StringBuilder();
        StringBuilder respondent = new StringBuilder();
        StringBuilder claimant = new StringBuilder();
        StringBuilder claimantRepresentative = new StringBuilder();
        StringBuilder prosecutingAuthority = new StringBuilder();

        hearing.get(PARTY).forEach(party -> {
            if (!GeneralHelper.findAndReturnNodeText(party, PARTY_ROLE).isEmpty()) {
                switch (PartyRoleMapper.convertPartyRole(party.get(PARTY_ROLE).asText())) {
                    case "APPLICANT_PETITIONER" ->
                        formatPartyNonRepresentative(party, applicant, initialised);
                    case "APPLICANT_PETITIONER_REPRESENTATIVE" ->
                        applicant.append(formatPartyRepresentative(language, party, initialised));
                    case "RESPONDENT" -> {
                        formatPartyNonRepresentative(party, respondent, initialised);
                        formatPartyNonRepresentative(party, prosecutingAuthority, initialised);
                    }
                    case "RESPONDENT_REPRESENTATIVE" ->
                        respondent.append(formatPartyRepresentative(language, party, initialised));
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

    private static String formatPartyRepresentative(Language language, JsonNode party,
                                                   boolean initialised) {
        final String details = createIndividualDetails(party, initialised);
        return formatPartyRepresentative(language, party, details);
    }

    public static String formatPartyRepresentative(Language language, JsonNode party, String details) {
        StringBuilder builder = new StringBuilder();
        if (!party.isEmpty() && StringUtil.isNotBlank(details)) {
            builder
                .append(language == Language.ENGLISH ? "Legal Advisor: " : "Cynghorydd Cyfreithiol: ")
                .append(details)
                .append(", ");
        }
        return builder.toString();
    }

    private static void formatPartyNonRepresentative(JsonNode party, StringBuilder builder, boolean initialised) {
        String details = createIndividualDetails(party, initialised);
        formatPartyNonRepresentative(builder, details);
    }

    public static void formatPartyNonRepresentative(StringBuilder builder, String details) {
        String result = details + GeneralHelper.stringDelimiter(details, ", ");
        builder.insert(0, result);
    }

    public static String createIndividualDetails(JsonNode party, boolean initialised) {
        if (party.has(INDIVIDUAL_DETAILS)) {
            JsonNode individualDetails = party.get(INDIVIDUAL_DETAILS);
            if (initialised) {
                String forename = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_FORENAMES);
                String forenameInitial = forename.isEmpty() ? "" : forename.substring(0, 1);
                return (GeneralHelper.findAndReturnNodeText(individualDetails, TITLE) + " "
                    + forenameInitial + " "
                    + GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_SURNAME)).trim();
            } else {
                return (GeneralHelper.findAndReturnNodeText(individualDetails, TITLE) + " "
                    + GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_FORENAMES) + " "
                    + GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_MIDDLE_NAME) + " "
                    + GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_SURNAME)).trim();
            }
        }
        return "";
    }

    private static String createIndividualDetails(JsonNode party) {
        JsonNode individualDetails = party.get(INDIVIDUAL_DETAILS);
        String forenames = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_FORENAMES);
        String surname = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_SURNAME);

        return surname + (surname.isEmpty() || forenames.isEmpty() ? "" : ", ")
            + forenames;
    }

    public static void handleParties(JsonNode hearing) {
        List<String> defendants = new ArrayList<>();
        List<String> defendantRepresentatives = new ArrayList<>();
        List<String> prosecutingAuthorities = new ArrayList<>();

        if (hearing.has(PARTY)) {
            hearing.get(PARTY).forEach(party -> {
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
