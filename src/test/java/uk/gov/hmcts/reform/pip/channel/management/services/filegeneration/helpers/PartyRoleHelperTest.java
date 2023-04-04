package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
class PartyRoleHelperTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PARTY = "party";

    private JsonNode loadInPartyFile() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(
            Paths.get("src/test/resources/mocks/partyManipulation.json")), writer,
                     Charset.defaultCharset()
        );
        return OBJECT_MAPPER.readTree(writer.toString());
    }

    @Test
    void testFindManipulatePartyInformationApplicant() throws IOException {
        JsonNode inputJson = loadInPartyFile();

        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, Language.ENGLISH, false);

        assertThat(inputJson.get("applicant").asText())
            .as("applicant is incorrect")
            .startsWith("Applicant Title Applicant Forename Applicant Middlename Applicant Surname");
    }

    @Test
    void testFindManipulatePartyInformationApplicantRepresentativeEnglish() throws IOException {
        JsonNode inputJson = loadInPartyFile();

        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, Language.ENGLISH, false);

        assertThat(inputJson.get("applicant").asText())
            .as("applicant is incorrect")
            .contains("Legal Advisor: Rep Title Rep Forename Rep Middlename Rep Surname");
    }

    @Test
    void testFindManipulatePartyInformationApplicantRepresentativeWelsh() throws IOException {
        JsonNode inputJson = loadInPartyFile();

        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, Language.WELSH, false);

        assertThat(inputJson.get("applicant").asText())
            .as("applicant is incorrect")
            .contains("Cynghorydd Cyfreithiol: Rep Title Rep Forename Rep Middlename Rep Surname");
    }

    @Test
    void testFindManipulatePartyInformationRespondent() throws IOException {
        JsonNode inputJson = loadInPartyFile();

        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, Language.ENGLISH, false);

        assertThat(inputJson.get("respondent").asText())
            .as("respondent is incorrect")
            .startsWith("Title Forename Middlename Surname");
    }

    @Test
    void testFindManipulatePartyInformationProsecutingAuthority() throws IOException {
        JsonNode inputJson = loadInPartyFile();

        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, Language.ENGLISH, false);

        assertThat(inputJson.get("prosecutingAuthority").asText())
            .as("prosecuting authority is incorrect")
            .isEqualTo("Title Forename Middlename Surname");
    }

    @Test
    void testFindManipulatePartyInformationRespondentRepresentative() throws IOException {
        JsonNode inputJson = loadInPartyFile();

        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, Language.ENGLISH, false);

        assertThat(inputJson.get("respondent").asText())
            .as("respondent is incorrect")
            .endsWith("Legal Advisor: Mr ForenameB MiddlenameB SurnameB");
    }

    @Test
    void testFindManipulatePartyInformationClaimant() throws IOException {
        JsonNode inputJson = loadInPartyFile();

        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, Language.ENGLISH, false);

        assertThat(inputJson.get("claimant").asText())
            .as("claimant is incorrect")
            .isEqualTo("Claimant Title Claimant Forename Claimant Middlename Claimant Surname");
    }

    @Test
    void testFindManipulatePartyInformationClaimantRepresentative() throws IOException {
        JsonNode inputJson = loadInPartyFile();

        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, Language.ENGLISH, false);

        assertThat(inputJson.get("claimantRepresentative").asText())
            .as("claimant representative is incorrect")
            .isEqualTo("Rep Title Rep Forename Rep Middlename Rep Surname");
    }

    @Test
    void testFormatPartyRepresentativeInEnglish() throws IOException {
        JsonNode inputJson = loadInPartyFile();

        assertThat(PartyRoleHelper.formatPartyRepresentative(Language.ENGLISH, inputJson.get(PARTY).get(1),
                                                             "forename surname"))
            .as("Party representative incorrect")
            .isEqualTo("Legal Advisor: forename surname, ");
    }

    @Test
    void testFormatPartyRepresentativeInWelsh() throws IOException {
        JsonNode inputJson = loadInPartyFile();

        assertThat(PartyRoleHelper.formatPartyRepresentative(Language.WELSH, inputJson.get(PARTY).get(1),
                                                             "forename surname"))
            .as("Party representative incorrect")
            .isEqualTo("Cynghorydd Cyfreithiol: forename surname, ");
    }

    @Test
    void testFormatPartyRepresentativeWithEmptyString() throws IOException {
        JsonNode inputJson = loadInPartyFile();

        assertThat(PartyRoleHelper.formatPartyRepresentative(Language.ENGLISH, inputJson.get(PARTY).get(1),
                                                             ""))
            .as("Party representative incorrect")
            .isEmpty();
    }

    @Test
    void testFormatPartyNonRepresentative() throws IOException {
        StringBuilder builder = new StringBuilder("name1");

        PartyRoleHelper.formatPartyNonRepresentative(builder, "name2");
        assertThat(builder)
            .as("Party non representative incorrect")
            .hasToString("name2, name1");
    }

    @Test
    void testCreateIndividualDetails() throws IOException {
        JsonNode inputJson = loadInPartyFile();

        assertThat(PartyRoleHelper.createIndividualDetails(inputJson.get(PARTY).get(0), false))
            .as("Individual details incorrect")
            .isEqualTo("Applicant Title Applicant Forename Applicant Middlename Applicant Surname");
    }

    @Test
    void testCreateIndividualDetailsWithInitials() throws IOException {
        JsonNode inputJson = loadInPartyFile();

        assertThat(PartyRoleHelper.createIndividualDetails(inputJson.get(PARTY).get(0), true))
            .as("Individual details incorrect")
            .isEqualTo("Applicant Title A Applicant Surname");
    }

    @Test
    void testCreateIndividualDetailsWithOrgInformation() throws IOException {
        JsonNode inputJson = loadInPartyFile();

        assertThat(PartyRoleHelper.createIndividualDetails(inputJson.get(PARTY).get(8), false))
            .as("Individual details should be blank")
            .isEmpty();
    }
}
