package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class FamilyMixedListHelperTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String CASE = "case";
    private static final String CASE_NAME = "caseName";
    private static final String CASE_TYPE = "caseType";
    private static final String APPLICANT = "applicant";
    private static final String APPLICANT_REPRESENTATIVE = "applicantRepresentative";
    private static final String RESPONDENT = "respondent";
    private static final String RESPONDENT_REPRESENTATIVE = "respondentRepresentative";

    private static JsonNode inputJson;

    @BeforeAll
    static void setup()  throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/familyDailyCauseList.json")),
                     writer, Charset.defaultCharset());
        inputJson = OBJECT_MAPPER.readTree(writer.toString());
    }

    @Test
    void testFormatCourtRoomName() {
        FamilyMixedListHelper.manipulatedlistData(inputJson, Language.ENGLISH);

        assertThat(inputJson.get(COURT_LISTS).get(0)
                       .get(COURT_HOUSE)
                       .get(COURT_ROOM).get(0)
                       .get(SESSION).get(0)
                       .get("formattedSessionCourtRoom").asText())
            .as("Unable to get courtroom name")
            .isEqualTo("This is the court room name");
    }

    @Test
    void testFormatHearingDuration() {
        FamilyMixedListHelper.manipulatedlistData(inputJson, Language.ENGLISH);

        assertThat(inputJson.get(COURT_LISTS).get(0)
                       .get(COURT_HOUSE)
                       .get(COURT_ROOM).get(0)
                       .get(SESSION).get(0)
                       .get(SITTINGS).get(0)
                       .get("formattedDuration").asText())
            .as("Unable to get duration")
            .isEqualTo("1 hour 25 mins");
    }

    @Test
    void testFormatHearingTime() {
        FamilyMixedListHelper.manipulatedlistData(inputJson, Language.ENGLISH);

        assertThat(inputJson.get(COURT_LISTS).get(0)
                       .get(COURT_HOUSE)
                       .get(COURT_ROOM).get(0)
                       .get(SESSION).get(0)
                       .get(SITTINGS).get(0)
                       .get("time").asText())
            .as("Unable to get hearing time")
            .isEqualTo("10:30am");
    }

    @Test
    void testFormatHearingChannel() {
        FamilyMixedListHelper.manipulatedlistData(inputJson, Language.ENGLISH);

        assertThat(inputJson.get(COURT_LISTS).get(0)
                       .get(COURT_HOUSE)
                       .get(COURT_ROOM).get(0)
                       .get(SESSION).get(0)
                       .get(SITTINGS).get(0)
                       .get("caseHearingChannel").asText())
            .as("Unable to get case hearing channel")
            .isEqualTo("Teams, Attended");
    }

    @Test
    void testFormatCaseName() {
        FamilyMixedListHelper.manipulatedlistData(inputJson, Language.ENGLISH);

        assertThat(inputJson.get(COURT_LISTS).get(0)
                       .get(COURT_HOUSE)
                       .get(COURT_ROOM).get(0)
                       .get(SESSION).get(0)
                       .get(SITTINGS).get(0)
                       .get(HEARING).get(0)
                       .get(CASE).get(0)
                       .get(CASE_NAME).asText())
            .as("Unable to get case name")
            .contains("[2 of 3]");
    }

    @Test
    void testCaseType() {
        FamilyMixedListHelper.manipulatedlistData(inputJson, Language.ENGLISH);

        assertThat(inputJson.get(COURT_LISTS).get(0)
                       .get(COURT_HOUSE)
                       .get(COURT_ROOM).get(0)
                       .get(SESSION).get(0)
                       .get(SITTINGS).get(0)
                       .get(HEARING).get(0)
                       .get(CASE).get(0)
                       .get(CASE_TYPE).asText())
            .as("Unable to get case type")
            .isEqualTo("normal");
    }

    @Test
    void testGetPartyWithIndividualDetails() {
        FamilyMixedListHelper.manipulatedlistData(inputJson, Language.ENGLISH);

        JsonNode hearing = inputJson.get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0)
            .get(SITTINGS).get(0)
            .get(HEARING).get(0);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(hearing.get(APPLICANT).asText())
            .as("Applicant is incorrect")
            .isEqualTo("Surname");

        softly.assertThat(hearing.get(APPLICANT_REPRESENTATIVE).asText())
            .as("Applicant representative is incorrect")
            .isEqualTo("Mr Individual Forenames Individual Middlename Individual Surname");

        softly.assertThat(hearing.get(RESPONDENT).asText())
            .as("Respondent is incorrect")
            .isEqualTo("Surname");

        softly.assertThat(hearing.get(RESPONDENT_REPRESENTATIVE).asText())
            .as("Respondent representative is incorrect")
            .isEmpty();

        softly.assertAll();
    }

    @Test
    void testGetPartyWithOrganisationDetails() {
        FamilyMixedListHelper.manipulatedlistData(inputJson, Language.ENGLISH);

        JsonNode hearing = inputJson.get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0)
            .get(SITTINGS).get(0)
            .get(HEARING).get(1);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(hearing.get(APPLICANT).asText())
            .as("Applicant is incorrect")
            .isEqualTo("Applicant org name");

        softly.assertThat(hearing.get(APPLICANT_REPRESENTATIVE).asText())
            .as("Applicant representative is incorrect")
            .isEqualTo("Applicant rep org name");

        softly.assertThat(hearing.get(RESPONDENT).asText())
            .as("Respondent is incorrect")
            .isEqualTo("Respondent org name");

        softly.assertThat(hearing.get(RESPONDENT_REPRESENTATIVE).asText())
            .as("Respondent representative is incorrect")
            .isEqualTo("Respondent rep org name");

        softly.assertAll();
    }
}
