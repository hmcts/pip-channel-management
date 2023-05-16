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
class CrownFirmListHelperTest {
    private static JsonNode inputJson;
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String PROVENANCE = "provenance";
    Map<String, Object> language =
        Map.of("rep", "Rep: ",
               "noRep", "Rep: ",
               "legalAdvisor", "Legal Advisor: ");

    Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                             PROVENANCE, PROVENANCE,
                                             "locationName", "location",
                                             "language", "ENGLISH"
    );

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths
                    .get("src/test/resources/mocks/crownFirmList.json")), writer,
                     Charset.defaultCharset()
        );

        inputJson = new ObjectMapper().readTree(writer.toString());
    }

    @Test
    void testCrownFirmListFormattedMethod() {
        preprocessArtefactForThymeLeafConverter(inputJson, metadataMap, language, true);
        CrownFirmListHelper.crownFirmListFormatted(inputJson);

        assertEquals(2, inputJson.get(COURT_LISTS).size(),
                     "Unable to find correct court List array");

        JsonNode sitting = inputJson
            .get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0)
            .get(SITTINGS).get(0);

        JsonNode hearing = sitting
            .get(HEARING).get(0);

        assertEquals("Wednesday 12 April 2023", sitting.get("sittingDate").asText(),
                     "Unable to find sitting date");

        assertEquals("9:35am", hearing.get("time").asText(),
                     "Unable to find time");

        assertEquals("1 hour 25 mins", hearing.get("formattedDuration").asText(),
                     "Unable to find duration");

        assertEquals("I4Y416QE", hearing.get("caseReference").asText(),
                     "Unable to find case number");

        assertEquals("Butcher, Pat", hearing.get("defendant").asText(),
                     "Unable to find defendant");

        assertEquals("Boyce Daniel", hearing.get("defendantRepresentative").asText(),
                     "Unable to find defendant representative");

        assertEquals("HMCTS", hearing.get("prosecutingAuthority").asText(),
                     "Unable to find prosecuting authority");

        assertEquals("Directions", hearing.get("hearingType").asText(),
                     "Unable to find hearing type");

        assertEquals("YRYCTRR3", hearing.get("linkedCases").asText(),
                     "Unable to find linked cases");

        assertEquals("Listing details text",
                     inputJson
                         .get(COURT_LISTS).get(0)
                         .get(COURT_HOUSE)
                         .get(COURT_ROOM).get(1)
                         .get(SESSION).get(0)
                         .get(SITTINGS).get(0)
                         .get(HEARING).get(0)
                         .get("listingNotes").asText(),
                     "Unable to find listing notes");
    }

    @Test
    void testSplitByCourtAndDateMethod() {
        preprocessArtefactForThymeLeafConverter(inputJson, metadataMap, language, true);
        CrownFirmListHelper.crownFirmListFormatted(inputJson);
        CrownFirmListHelper.splitByCourtAndDate(inputJson);

        assertEquals(2, inputJson.get(COURT_LISTS).size(),
                     "Unable to find correct court List array");

        assertEquals("Wednesday 12 April 2023",
                     inputJson
                         .get("courtListsByDate").get(0).get(0)
                         .get("courtSittingDate").asText(),
                     "Unable to find court sitting date");

        assertEquals("Courtroom 2: Thomas Athorne, Reginald Cork",
                     inputJson
                         .get("courtListsByDate").get(0).get(0)
                         .get("courtRooms").get(0)
                         .get("formattedSessionCourtRoom").asText(),
                     "Unable to find court room name");
    }
}
