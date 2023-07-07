package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.CourtHouse;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.CourtRoom;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.Hearing;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.Sitting;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class SscsListHelperTest {
    private static final String COURT_LISTS = "courtLists";

    private static JsonNode inputCourtHouse;
    private static JsonNode inputCourtHouseWithProsecutors;

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/sscsDailyList.json")), writer,
                     Charset.defaultCharset()
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        inputCourtHouse = inputJson.get(COURT_LISTS).get(0);
        inputCourtHouseWithProsecutors = inputJson.get(COURT_LISTS).get(2);
    }

    @Test
    void testSscsCourtHouse() throws JsonProcessingException {
        CourtHouse courtHouse = SscsListHelper.courtHouseBuilder(inputCourtHouse);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(courtHouse.getName())
            .as("Court house name does not match")
            .isEqualTo("Reading Crown Court");

        softly.assertThat(courtHouse.getPhone())
            .as("Court house phone number does not match")
            .isEqualTo("01234 123 123");

        softly.assertThat(courtHouse.getEmail())
            .as("Court house email does not match")
            .isEqualTo("a@b.com");

        softly.assertThat(courtHouse.getListOfCourtRooms())
                .hasSize(1);

        softly.assertAll();
    }

    @Test
    void testSscsCourtRoom() throws JsonProcessingException {
        CourtRoom courtRoom = SscsListHelper.courtHouseBuilder(inputCourtHouse)
            .getListOfCourtRooms()
            .get(0);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(courtRoom.getName())
            .as("Court room name does nopt match")
            .isEqualTo("Court room 1");

        softly.assertThat(courtRoom.getListOfSittings())
            .as("Court room sitting count does not match")
            .hasSize(1);

        softly.assertAll();
    }

    @Test
    void testSscsSitting() throws JsonProcessingException {
        Sitting sitting = SscsListHelper.courtHouseBuilder(inputCourtHouse)
            .getListOfCourtRooms().get(0)
            .getListOfSittings().get(0);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(sitting.getSittingStart())
            .as("Sitting start does not match")
            .isNull();

        softly.assertThat(sitting.getChannel())
            .as("Channel does not match")
            .isEqualTo("Teams, Attended");

        softly.assertThat(sitting.getJudiciary())
            .as("Judiciary does not match")
            .isEqualTo("Judge Judy, Magistrate Statham");

        softly.assertThat(sitting.getListOfHearings())
            .as("Hearing count does not match")
            .hasSize(1);

        softly.assertAll();
    }

    @Test
    void testSscsHearing() throws JsonProcessingException {
        Hearing hearing = SscsListHelper.courtHouseBuilder(inputCourtHouse)
            .getListOfCourtRooms().get(0)
            .getListOfSittings().get(0)
            .getListOfHearings().get(0);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(hearing.getHearingTime())
            .as("Hearing time does not match")
            .isEqualTo("12:30am");

        softly.assertThat(hearing.getAppealRef())
            .as("Appeal reference does not match")
            .isEqualTo("12341234");

        softly.assertThat(hearing.getTribunalType())
            .as("Tribunal type does not match")
            .isNull();

        softly.assertThat(hearing.getAppellant())
            .as("Appellant does not match")
            .isEqualTo("Lovekesh");

        softly.assertThat(hearing.getAppellantRepresentative())
            .as("Appellant representative does not match")
            .isEqualTo("Mr Sausage Alpha Foxtrot");

        softly.assertThat(hearing.getRespondent())
            .as("Respondent does not match")
            .isEqualTo("NVQ, SQA");

        softly.assertThat(hearing.getJudiciary())
            .as("Judiciary does not match")
            .isEqualTo("Judge Judy, Magistrate Statham");

        softly.assertAll();
    }

    @Test
    void testFormatRespondentWithNoInformants() throws JsonProcessingException {
        Hearing hearing = SscsListHelper.courtHouseBuilder(inputCourtHouseWithProsecutors)
            .getListOfCourtRooms().get(0)
            .getListOfSittings().get(0)
            .getListOfHearings().get(0);

        assertThat(hearing.getRespondent())
            .as("Party prosecutor does not match")
            .isEqualTo("Prosecutor1, Prosecutor2");
    }
}
