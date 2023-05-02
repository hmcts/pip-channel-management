package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.CommonListHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
class CrimeListHelperTest {
    private static JsonNode inputJsonCrownDailyList;
    private static JsonNode inputJsonMagistratesPublicList;
    private static JsonNode partyRoleJson;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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
    private static final String PARTY_NAME_MESSAGE = "Party name do not match";

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

        StringWriter partyRoleWriter = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/partyManipulation.json")),
                     partyRoleWriter, Charset.defaultCharset()
        );

        inputJsonCrownDailyList = OBJECT_MAPPER.readTree(crownDailyWriter.toString());
        inputJsonMagistratesPublicList = OBJECT_MAPPER.readTree(magistratesPublicWriter.toString());
        partyRoleJson = OBJECT_MAPPER.readTree(partyRoleWriter.toString());
    }

    @Test
    void testFindUnallocatedCasesInCrownDailyListDataMethod() {
        assertEquals(4, inputJsonCrownDailyList.get(COURT_LISTS).size(),
                     "Unable to find correct court List array");
        CrimeListHelper.findUnallocatedCasesInCrownDailyListData(inputJsonCrownDailyList);
        assertEquals(5, inputJsonCrownDailyList.get(COURT_LISTS).size(),
                     "Unable to find correct court List array when unallocated cases are there");
        assertTrue(inputJsonCrownDailyList.get(COURT_LISTS).get(4).get("unallocatedCases").asBoolean(),
                   "Unable to find unallocated case section");
        assertFalse(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get("unallocatedCases").asBoolean(),
                    "Unable to find allocated case section");
        assertTrue(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(1)
                       .get("exclude").asBoolean(),
                   "Unable to find unallocated courtroom");
        assertEquals("to be allocated", inputJsonCrownDailyList.get(COURT_LISTS).get(4).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get("courtRoomName").asText(),
                     "Unable to find unallocated courtroom");
    }

    @Test
    void testFormattedCourtRoomNameMethodCrownDailyList() {
        CommonListHelper.manipulatedListData(inputJsonCrownDailyList, Language.ENGLISH, false);
        CrimeListHelper.manipulatedCrimeListData(inputJsonCrownDailyList, ListType.CROWN_DAILY_LIST);
        CrimeListHelper.findUnallocatedCasesInCrownDailyListData(inputJsonCrownDailyList);
        CrimeListHelper.formattedCourtRoomName(inputJsonCrownDailyList);

        assertEquals("1: Firstname1 Surname1, Firstname2 Surname2", inputJsonCrownDailyList.get(COURT_LISTS)
                         .get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(FORMATTED_SESSION_COURT_ROOM).asText(),
                     "Unable to find formatted courtroom name");

        assertEquals("to be allocated", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(1).get(SESSION).get(0).get(FORMATTED_SESSION_COURT_ROOM).asText(),
                     "Unable to find unallocated formatted courtroom name");

        assertEquals("CourtRoom 1", inputJsonCrownDailyList.get(COURT_LISTS).get(1).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(FORMATTED_SESSION_COURT_ROOM).asText(),
                     "Unable to find formatted courtroom name without judge");
    }

    @Test
    void testManipulatedCrownDailyListDataMethod() {
        CrimeListHelper.manipulatedCrimeListData(inputJsonCrownDailyList, ListType.CROWN_DAILY_LIST);

        assertEquals("10:40am", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM)
                         .get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(TIME).asText(),
                     TIME_ERROR);
        assertEquals("1:00pm", inputJsonCrownDailyList.get(COURT_LISTS).get(2).get(COURT_HOUSE).get(COURT_ROOM)
                         .get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(TIME).asText(),
                     TIME_ERROR);
        assertEquals("Defendant_SN, Defendant_FN", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("defendant").asText(),
                     "Unable to find information for defendant");
        assertEquals("Pro_Auth", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM)
                         .get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("prosecutingAuthority").asText(),
                     "Unable to find information for prosecution authority");
        assertEquals("caseid111, caseid222", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(CASE).get(0).get("linkedCases").asText(),
                     "Unable to find linked cases for a particular case");
        assertEquals("", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0).get(CASE).get(1)
                         .get("linkedCases").asText(),
                     "able to find linked cases for a particular case");
        assertEquals("Listing details text", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(LISTING_NOTES).asText(),
                     "Unable to find listing notes for a particular hearing");
        assertEquals("", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(1).get(LISTING_NOTES).asText(),
                     "Able to find listing notes for a particular hearing");
        assertEquals("no-border-bottom", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(CASE).get(0).get("caseCellBorder").asText(),
                     "Unable to find linked cases css for a particular case");
        assertEquals("no-border-bottom", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(CASE).get(0).get("linkedCasesBorder").asText(),
                     "Unable to find linked cases css for a particular case");
    }

    @Test
    void testManipulatedMagistratesPublicListDataMethod() {
        CrimeListHelper.manipulatedCrimeListData(inputJsonMagistratesPublicList, ListType.MAGISTRATES_PUBLIC_LIST);

        assertEquals("10:40am", inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(TIME).asText(),
                     TIME_ERROR);
        assertEquals("1:00pm", inputJsonMagistratesPublicList.get(COURT_LISTS).get(2).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(TIME).asText(),
                     TIME_ERROR);
        assertEquals("Defendant_SN, Defendant_FN", inputJsonMagistratesPublicList.get(COURT_LISTS).get(0)
                         .get(COURT_HOUSE).get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING)
                         .get(0).get("defendant").asText(),
                     "Unable to find information for defendant");
        assertEquals("Pro_Auth", inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get("prosecutingAuthority").asText(),
                     "Unable to find information for prosecution authority");
        assertEquals("Listing details text", inputJsonMagistratesPublicList.get(COURT_LISTS).get(0)
                         .get(COURT_HOUSE).get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING)
                         .get(0).get(LISTING_NOTES).asText(),
                     "Unable to find listing notes for a particular hearing");
        assertEquals("", inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM)
                         .get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(1).get(LISTING_NOTES)
                         .asText(),
                     "Able to find listing notes for a particular hearing");

    }

    @Test
    void testFormattedCourtRoomNameMethodMagistratesPublicList() {
        CommonListHelper.manipulatedListData(inputJsonMagistratesPublicList, Language.ENGLISH, false);
        CrimeListHelper.manipulatedCrimeListData(inputJsonMagistratesPublicList, ListType.MAGISTRATES_PUBLIC_LIST);
        CrimeListHelper.formattedCourtRoomName(inputJsonMagistratesPublicList);

        assertEquals("1: Firstname1 Surname1, Firstname2 Surname2", inputJsonMagistratesPublicList
                         .get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(FORMATTED_SESSION_COURT_ROOM).asText(),
                     "Unable to find formatted courtroom name");

        assertEquals("to be allocated", inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(1).get(SESSION).get(0).get(FORMATTED_SESSION_COURT_ROOM).asText(),
                     "Unable to find unallocated formatted courtroom name");

        assertEquals("CourtRoom 1", inputJsonMagistratesPublicList.get(COURT_LISTS).get(1).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(FORMATTED_SESSION_COURT_ROOM).asText(),
                     "Unable to find formatted courtroom name without judge");
    }

    @Test
    void testHandleDefendantParty() {
        PartyRoleHelper.handleParties(partyRoleJson);
        assertThat(partyRoleJson.get("defendant").asText())
            .as(PARTY_NAME_MESSAGE)
            .isEqualTo("SurnameA, ForenamesA, SurnameB, ForenamesB");
    }

    @Test
    void testHandleDefendantRepresentativeParty() {
        PartyRoleHelper.handleParties(partyRoleJson);
        assertThat(partyRoleJson.get("defendantRepresentative").asText())
            .as(PARTY_NAME_MESSAGE)
            .isEqualTo("Defendant rep name");
    }

    @Test
    void testHandleProsecutingAuthorityParty() {
        PartyRoleHelper.handleParties(partyRoleJson);
        assertThat(partyRoleJson.get("prosecutingAuthority").asText())
            .as(PARTY_NAME_MESSAGE)
            .isEqualTo("Prosecuting authority name");
    }
}

