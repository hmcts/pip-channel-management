package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

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
import static uk.gov.hmcts.reform.pip.channel.management.services.helpers.CommonListHelper.preprocessArtefactForThymeLeafConverter;

@ActiveProfiles("test")
class MagistratesStandardListHelperTest {
    private static JsonNode inputJson;

    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String DEFENDANTS = "defendants";
    private static final String DEFENDANT_INFO = "defendantInfo";
    private static final String PROVENANCE = "provenance";

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

        JsonNode defendant = inputJson.get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0)
            .get(DEFENDANTS).get(0);

        JsonNode defendantInfo = defendant.get(DEFENDANT_INFO).get(0);

        assertEquals("Surname1, John Smith1 (male)", defendant.get("defendantHeading").asText(),
                     "Unable to defendant heading");

        assertEquals("1", defendantInfo.get("sittingSequence").asText(),
                     "Unable to find sitting sequence");

        assertEquals("2 hours 30 mins", defendantInfo.get("formattedDuration").asText(),
                     "Unable to find formatted duration");

        assertEquals("01/01/1983 Age: 39", defendantInfo.get("defendantDateOfBirthAndAge").asText(),
                     "Unable to find date of birth and age");

        assertEquals("Address Line 1, Address Line 2, Month A, County A, AA1 AA1",
                     defendantInfo.get("defendantAddress").asText(),
                     "Unable to find address");

        assertEquals("Test1234", defendantInfo.get("prosecutionAuthorityCode").asText(),
                     "Unable to find prosecution authority code");

        assertEquals("12", defendantInfo.get("hearingNumber").asText(),
                     "Unable to find hearing number");

        assertEquals("VIDEO HEARING", defendantInfo.get("caseHearingChannel").asText(),
                     "Unable to find case hearing channel");

        assertEquals("45684548", defendantInfo.get("caseNumber").asText(),
                     "Unable to find case number");

        assertEquals("mda", defendantInfo.get("hearingType").asText(),
                     "Unable to find hearing type");

        assertEquals("ADULT", defendantInfo.get("panel").asText(),
                     "Unable to find panel");
    }

    @Test
    void testOffenceMagistratesStandardListMethod() {
        preprocessArtefactForThymeLeafConverter(inputJson, metadataMap, language, true);
        MagistratesStandardListHelper.manipulatedMagistratesStandardList(inputJson, language);

        JsonNode defendantInfo = inputJson.get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0)
            .get(DEFENDANTS).get(0).get(DEFENDANT_INFO).get(0);

        assertEquals("1. drink driving", defendantInfo.get("offence").get(0).get("offenceTitle").asText(),
                     "Unable to find offence title");

        assertEquals("NOT_GUILTY", defendantInfo.get("plea").asText(),
                     "Unable to find plea");

        assertEquals("14/09/2016", defendantInfo.get("formattedConvictionDate").asText(),
                     "Unable to find formatted conviction date");

        assertEquals("14/09/2016", defendantInfo.get("formattedAdjournedDate").asText(),
                     "Unable to find formatted adjourned date");
    }
}
