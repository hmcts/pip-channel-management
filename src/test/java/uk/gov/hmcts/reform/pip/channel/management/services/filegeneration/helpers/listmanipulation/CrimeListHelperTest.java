package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.ListType;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
class CrimeListHelperTest {
    private static JsonNode inputJsonCrownDailyList;
    private static JsonNode inputJsonMagistratesPublicList;
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String CASE = "case";
    private static final String FORMATTED_SESSION_COURT_ROOM = "formattedSessionCourtRoom";
    private static final String TIME = "time";
    private static final String LISTING_NOTES = "listingNotes";
    private static final String TIME_ERROR = "Unable to find correct case time";

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter crownDailyWriter = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/crownDailyList.json")),
                     crownDailyWriter, Charset.defaultCharset()
        );

        StringWriter magistratesPublicWriter = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/magistratesPublicList.json")),
                     magistratesPublicWriter, Charset.defaultCharset()
        );

        inputJsonCrownDailyList = new ObjectMapper().readTree(crownDailyWriter.toString());
        inputJsonMagistratesPublicList = new ObjectMapper().readTree(magistratesPublicWriter.toString());
    }

    @Test
    void testFindUnallocatedCasesInCrownDailyListDataMethod() {
        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).size(), 4,
                     "Unable to find correct court List array");
        CrimeListHelper.findUnallocatedCasesInCrownDailyListData(inputJsonCrownDailyList);
        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).size(), 5,
                     "Unable to find correct court List array when unallocated cases are there");
        assertTrue(inputJsonCrownDailyList.get(COURT_LISTS).get(4).get("unallocatedCases").asBoolean(),
                   "Unable to find unallocated case section");
        assertFalse(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get("unallocatedCases").asBoolean(),
                    "Unable to find allocated case section");
        assertTrue(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(1)
                       .get("exclude").asBoolean(),
                   "Unable to find unallocated courtroom");
        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).get(4).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get("courtRoomName").asText(), "to be allocated",
                     "Unable to find unallocated courtroom");
    }

    @Test
    void testFormattedCourtRoomNameMethodCrownDailyList() {
        DataManipulation.manipulatedDailyListData(inputJsonCrownDailyList, Language.ENGLISH, false);
        CrimeListHelper.manipulatedCrimeListData(inputJsonCrownDailyList, ListType.CROWN_DAILY_LIST);
        CrimeListHelper.findUnallocatedCasesInCrownDailyListData(inputJsonCrownDailyList);
        CrimeListHelper.formattedCourtRoomName(inputJsonCrownDailyList);

        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(FORMATTED_SESSION_COURT_ROOM).asText(),
                     "1: Firstname1 Surname1, Firstname2 Surname2",
                     "Unable to find formatted courtroom name");

        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(1)
                         .get(SESSION).get(0).get(FORMATTED_SESSION_COURT_ROOM).asText(), "to be allocated",
                     "Unable to find unallocated formatted courtroom name");

        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).get(1).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(FORMATTED_SESSION_COURT_ROOM).asText(), "CourtRoom 1",
                     "Unable to find formatted courtroom name without judge");
    }

    @Test
    void testManipulatedCrownDailyListDataMethod() {
        CrimeListHelper.manipulatedCrimeListData(inputJsonCrownDailyList, ListType.CROWN_DAILY_LIST);

        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(TIME).asText(), "10:40am",
                     TIME_ERROR);
        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).get(2).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(TIME).asText(), "1:00pm",
                     TIME_ERROR);
        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("defendant").asText(), "Defendant_SN, Defendant_FN",
                     "Unable to find information for defendant");
        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("prosecutingAuthority").asText(), "Pro_Auth",
                     "Unable to find information for prosecution authority");
        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(CASE).get(0).get("linkedCases").asText(), "caseid111, caseid222",
                     "Unable to find linked cases for a particular case");
        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(CASE).get(1).get("linkedCases").asText(), "",
                     "able to find linked cases for a particular case");
        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(LISTING_NOTES).asText(), "Listing details text",
                     "Unable to find listing notes for a particular hearing");
        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(1)
                         .get(LISTING_NOTES).asText(), "",
                     "Able to find listing notes for a particular hearing");
        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(CASE).get(0).get("caseCellBorder").asText(), "no-border-bottom",
                     "Unable to find linked cases css for a particular case");
        assertEquals(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(CASE).get(0).get("linkedCasesBorder").asText(), "no-border-bottom",
                     "Unable to find linked cases css for a particular case");
    }

    @Test
    void testManipulatedMagistratesPublicListDataMethod() {
        CrimeListHelper.manipulatedCrimeListData(inputJsonMagistratesPublicList, ListType.MAGISTRATES_PUBLIC_LIST);

        assertEquals(inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(TIME).asText(), "10:40am",
                     TIME_ERROR);
        assertEquals(inputJsonMagistratesPublicList.get(COURT_LISTS).get(2).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(TIME).asText(), "1:00pm",
                     TIME_ERROR);
        assertEquals(inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("defendant").asText(), "Defendant_SN, Defendant_FN",
                     "Unable to find information for defendant");
        assertEquals(inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("prosecutingAuthority").asText(), "Pro_Auth",
                     "Unable to find information for prosecution authority");
        assertEquals(inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(LISTING_NOTES).asText(), "Listing details text",
                     "Unable to find listing notes for a particular hearing");
        assertEquals(inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(1)
                         .get(LISTING_NOTES).asText(), "",
                     "Able to find listing notes for a particular hearing");

    }

    @Test
    void testFormattedCourtRoomNameMethodMagistratesPublicList() {
        DataManipulation.manipulatedDailyListData(inputJsonMagistratesPublicList, Language.ENGLISH, false);
        CrimeListHelper.manipulatedCrimeListData(inputJsonMagistratesPublicList, ListType.MAGISTRATES_PUBLIC_LIST);
        CrimeListHelper.formattedCourtRoomName(inputJsonMagistratesPublicList);

        assertEquals(inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(FORMATTED_SESSION_COURT_ROOM).asText(),
                     "1: Firstname1 Surname1, Firstname2 Surname2",
                     "Unable to find formatted courtroom name");

        assertEquals(inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(1)
                         .get(SESSION).get(0).get(FORMATTED_SESSION_COURT_ROOM).asText(), "to be allocated",
                     "Unable to find unallocated formatted courtroom name");

        assertEquals(inputJsonMagistratesPublicList.get(COURT_LISTS).get(1).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(FORMATTED_SESSION_COURT_ROOM).asText(), "CourtRoom 1",
                     "Unable to find formatted courtroom name without judge");
    }
}

