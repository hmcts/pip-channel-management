package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DailyCauseListHelper.preprocessArtefactForThymeLeafConverter;

@ActiveProfiles("test")
class MagistratesStandardListHelperTest {
    private static JsonNode inputJson;
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String DEFENDANTS = "defendants";
    private static final String DEFENDANT_INFO = "defendantInfo";

    public static final String PROVENANCE = "provenance";

    Map<String, Object> language =
        Map.of("age", "Age: ");

    Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                             PROVENANCE, PROVENANCE,
                                             "locationName", "location",
                                             "language", "ENGLISH"
    );

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
            "src/test/resources/mocks/magistratesStandardList.json")), writer,
                     Charset.defaultCharset()
        );

        inputJson = new ObjectMapper().readTree(writer.toString());
    }

    @Test
    void testManipulatedMagistratesStandardListMethod() {
        preprocessArtefactForThymeLeafConverter(inputJson, metadataMap, language, true);
        MagistratesStandardListHelper.manipulatedMagistratesStandardList(inputJson, language);

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get("defendantHeading").asText(),
                     "Surname1, John Smith1 (male)",
                     "Unable to defendant heading");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get(DEFENDANT_INFO).get(0)
                         .get("sittingSequence").asText(),
                     "1",
                     "Unable to find sitting sequence");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get(DEFENDANT_INFO).get(0)
                         .get("formattedDuration").asText(),
                     "2 hours 30 mins",
                     "Unable to find formatted duration");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get(DEFENDANT_INFO).get(0)
                         .get("defendantDateOfBirthAndAge").asText(),
                     "01/01/1983 Age: 39",
                     "Unable to find date of birth and age");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get(DEFENDANT_INFO).get(0)
                         .get("defendantAddress").asText(),
                     "Address Line 1, Address Line 2, Month A, County A, AA1 AA1",
                     "Unable to find address");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get(DEFENDANT_INFO).get(0)
                         .get("prosecutionAuthorityCode").asText(),
                     "Test1234",
                     "Unable to find prosecution authority code");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get(DEFENDANT_INFO).get(0)
                         .get("hearingNumber").asText(),
                     "12",
                     "Unable to find hearing number");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get(DEFENDANT_INFO).get(0)
                         .get("caseHearingChannel").asText(),
                     "VIDEO HEARING",
                     "Unable to hearing channel");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get(DEFENDANT_INFO).get(0)
                         .get("caseNumber").asText(),
                     "45684548",
                     "Unable to case number");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get(DEFENDANT_INFO).get(0)
                         .get("hearingType").asText(),
                     "mda",
                     "Unable to in hearing type");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get(DEFENDANT_INFO).get(0)
                         .get("panel").asText(),
                     "ADULT",
                     "Unable to find panel");

    }

    @Test
    void testOffenceMagistratesStandardListMethod() {
        preprocessArtefactForThymeLeafConverter(inputJson, metadataMap, language, true);
        MagistratesStandardListHelper.manipulatedMagistratesStandardList(inputJson, language);

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get(DEFENDANT_INFO).get(0)
                         .get("offence").get(0)
                         .get("offenceTitle").asText(),
                     "1. drink driving",
                     "Unable to find offence title");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get(DEFENDANT_INFO).get(0)
                         .get("plea").asText(),
                     "NOT_GUILTY",
                     "Unable to find plea");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get(DEFENDANT_INFO).get(0)
                         .get("formattedConvictionDate").asText(),
                     "14/09/2016",
                     "Unable to find formatted conviction date");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(DEFENDANTS).get(0)
                         .get(DEFENDANT_INFO).get(0)
                         .get("formattedAdjournedDate").asText(),
                     "14/09/2016",
                     "Unable to find formatted adjourned date");
    }
}
