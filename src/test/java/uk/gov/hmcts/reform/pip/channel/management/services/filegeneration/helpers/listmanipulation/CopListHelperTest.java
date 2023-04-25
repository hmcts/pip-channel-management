package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
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
class CopListHelperTest {
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String CASE = "case";

    private static JsonNode inputJson;

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/copDailyCauseList.json")), writer,
                     Charset.defaultCharset()
        );

        inputJson = new ObjectMapper().readTree(writer.toString());
    }

    @Test
    void testFormatCaseIndicator() {
        CopListHelper.manipulateCopListData(inputJson, Language.ENGLISH);

        assertThat(inputJson.get(COURT_LISTS).get(0)
                       .get(COURT_HOUSE)
                       .get(COURT_ROOM).get(0)
                       .get(SESSION).get(0)
                       .get(SITTINGS).get(0)
                       .get(HEARING).get(0)
                       .get(CASE).get(0)
                       .get("caseIndicator").asText())
            .as("Unable to get case name")
            .contains("[1 of 2]");
    }

    @Test
    void testFindAndManipulateJudiciary() {
        CopListHelper.manipulateCopListData(inputJson, Language.ENGLISH);

        assertThat(inputJson.get(COURT_LISTS).get(0)
                       .get(COURT_HOUSE)
                       .get(COURT_ROOM).get(0)
                       .get(SESSION).get(0)
                       .get("formattedSessionJoh").asText())
            .as("Unable to get session Joh")
            .contains("Mrs Firstname Surname");

        assertThat(inputJson.get(COURT_LISTS).get(1)
                       .get(COURT_HOUSE)
                       .get(COURT_ROOM).get(0)
                       .get(SESSION).get(0)
                       .get("formattedSessionJoh").asText())
            .as("Unable to get session Joh")
            .contains("Mrs Firstname Surname, Mrs OtherFirstname OtherSurname");
    }

    @Test
    void testFormatRegionNameWhenNotPresent() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(
                         Paths.get("src/test/resources/mocks/copDailyCauseListMissingRegion.json")), writer,
                     Charset.defaultCharset()
        );

        JsonNode newInputJson = new ObjectMapper().readTree(writer.toString());

        CopListHelper.manipulateCopListData(newInputJson, Language.ENGLISH);

        assertThat(newInputJson.get("regionName").asText())
            .as("Unable to get region name")
            .isEmpty();
    }

    @Test
    void testFormatRegionalJoh() {
        CopListHelper.manipulateCopListData(inputJson, Language.ENGLISH);

        assertThat(inputJson.get("regionalJoh").asText())
            .as("Unable to get regional Joh")
            .contains("Judge Firstname Surname");
    }

    @Test
    void testMissingRegionalJoh() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(
                         Paths.get("src/test/resources/mocks/copDailyCauseListMissingRegionalJoh.json")), writer,
                     Charset.defaultCharset()
        );

        JsonNode newInputJson = new ObjectMapper().readTree(writer.toString());

        CopListHelper.manipulateCopListData(newInputJson, Language.ENGLISH);

        assertThat(newInputJson.get("regionalJoh").asText())
            .as("Unable to get regional Joh")
            .isEmpty();
    }
}
