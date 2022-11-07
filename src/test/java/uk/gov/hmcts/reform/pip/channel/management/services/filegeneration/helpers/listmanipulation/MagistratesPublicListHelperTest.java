package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
class MagistratesPublicListHelperTest {
    private static JsonNode inputJson;
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/magistratesPublicList.json")),
                     writer, Charset.defaultCharset()
        );

        inputJson = new ObjectMapper().readTree(writer.toString());
    }

    @Test
    void testFormattedCourtRoomNameMethod() {
        DataManipulation.manipulatedDailyListData(inputJson, Language.ENGLISH, false);
        MagistratesPublicListHelper.manipulatedMagistratesPublicListData(inputJson);
        MagistratesPublicListHelper.formattedCourtRoomName(inputJson);

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get("formattedSessionCourtRoom").asText(),
                     "1: Firstname1 Surname1, Firstname2 Surname2",
                     "Unable to find formatted courtroom name");

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(1)
                         .get(SESSION).get(0).get("formattedSessionCourtRoom").asText(), "to be allocated",
                     "Unable to find unallocated formatted courtroom name");

        assertEquals(inputJson.get(COURT_LISTS).get(1).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get("formattedSessionCourtRoom").asText(), "CourtRoom 1",
                     "Unable to find formatted courtroom name without judge");
    }

    @Test
    void testManipulatedMagistratesPublicListDataMethod() {
        MagistratesPublicListHelper.manipulatedMagistratesPublicListData(inputJson);

        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get("time").asText(), "10:40am",
                     "Unable to find correct case time");
        assertEquals(inputJson.get(COURT_LISTS).get(2).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get("time").asText(), "1:00pm",
                     "Unable to find correct case time");
        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("defendant").asText(), "Defendant_SN, Defendant_FN",
                     "Unable to find information for defendant");
        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("prosecuting_authority").asText(), "Pro_Auth_SN, Pro_Auth_FN",
                     "Unable to find information for prosecution authority");
        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("listingNotes").asText(), "Listing details text",
                     "Unable to find listing notes for a particular hearing");
        assertEquals(inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(1)
                         .get("listingNotes").asText(), "",
                     "Able to find listing notes for a particular hearing");

    }
}

