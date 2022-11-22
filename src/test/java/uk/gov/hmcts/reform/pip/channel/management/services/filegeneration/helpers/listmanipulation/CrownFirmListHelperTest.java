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
class CrownFirmListHelperTest {
    private static JsonNode inputJson;
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    public static final String PROVENANCE = "provenance";
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

        assertEquals(inputJson.get(COURT_LISTS).size(), 2,
                     "Unable to find correct court List array");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(SITTINGS).get(0).get("sittingDate").asText(),
                     "Wednesday 12 April 2023",
                     "Unable to find sitting date");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("time").asText(),
                     "9:35am",
                     "Unable to find time");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("formattedDuration").asText(),
                     "1 hour 25 mins",
                     "Unable to find duration");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("caseReference").asText(),
                     "I4Y416QE",
                     "Unable to find case number");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("defendant").asText(),
                     "Butcher, Pat",
                     "Unable to find defendant");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("defendantRepresentative").asText(),
                     "Boyce Daniel",
                     "Unable to find defendant representative");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("prosecutingAuthority").asText(),
                     "HMCTS",
                     "Unable to find prosecuting authority");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("hearingType").asText(),
                     "Directions",
                     "Unable to find hearing type");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("linkedCases").asText(),
                     "YRYCTRR3",
                     "Unable to find linked cases");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(1).get(SESSION).get(0)
                         .get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("listingNotes").asText(),
                     "Listing details text",
                     "Unable to find listing notes");
    }

    @Test
    void testSplitByCourtAndDateMethod() {
        preprocessArtefactForThymeLeafConverter(inputJson, metadataMap, language, true);
        CrownFirmListHelper.crownFirmListFormatted(inputJson);
        CrownFirmListHelper.splitByCourtAndDate(inputJson);

        assertEquals(inputJson.get(COURT_LISTS).size(), 2,
                     "Unable to find correct court List array"
        );

        assertEquals(
            inputJson.get("courtListsByDate").get(0).get(0)
                .get("courtSittingDate").asText(),
            "Wednesday 12 April 2023",
            "Unable to find court sitting date"
        );

        assertEquals(
            inputJson.get("courtListsByDate").get(0).get(0)
                .get("courtRooms").get(0).get("formattedSessionCourtRoom").asText(),
            "Courtroom 2: Thomas Athorne, Reginald Cork",
            "Unable to find court room name"
        );
    }
}
