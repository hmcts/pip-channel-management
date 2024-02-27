package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
class CrimeListHelperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String CASE = "case";
    private static final String PARTY = "party";
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String ORGANISATION_DETAILS = "organisationDetails";
    private static final String ORGANISATION_ADDRESS = "organisationAddress";
    private static final String ADDRESS = "address";

    private static final String FORMATTED_SESSION_COURT_ROOM = "formattedSessionCourtRoom";

    private static JsonNode inputJsonCrownDailyList;

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter crownDailyWriter = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/crownDailyList.json")),
                     crownDailyWriter, Charset.defaultCharset()
        );

        inputJsonCrownDailyList = OBJECT_MAPPER.readTree(crownDailyWriter.toString());
    }

    @Test
    void testFormattedCourtRoomNameMethod() {
        CrownDailyListHelper.manipulatedCrownDailyListData(inputJsonCrownDailyList, Language.ENGLISH);
        CrownDailyListHelper.findUnallocatedCases(inputJsonCrownDailyList);

        assertEquals("1: Judge KnownAs, Judge KnownAs 2", inputJsonCrownDailyList.get(COURT_LISTS)
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
    void testFormatDefendantAddress() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/opaPressList.json")),
                     writer, Charset.defaultCharset()
        );
        JsonNode input = OBJECT_MAPPER.readTree(writer.toString());

        JsonNode defendantAddress = input.get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0)
            .get(SITTINGS).get(0)
            .get(HEARING).get(0)
            .get(CASE).get(0)
            .get(PARTY).get(0)
            .get(INDIVIDUAL_DETAILS)
            .get(ADDRESS);

        assertEquals("Address Line 1, Address Line 2, Town, County, BB1 1BB",
                     CrimeListHelper.formatDefendantAddress(defendantAddress),
                     "Defendant address does not match");
    }

    @Test
    void testFormatDefendantAddressIfPostcodeBlank() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/opaPressList.json")),
                     writer, Charset.defaultCharset()
        );
        JsonNode input = OBJECT_MAPPER.readTree(writer.toString());

        JsonNode defendantAddress = input.get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0)
            .get(SITTINGS).get(0)
            .get(HEARING).get(1)
            .get(CASE).get(0)
            .get(PARTY).get(1)
            .get(ORGANISATION_DETAILS)
            .get(ORGANISATION_ADDRESS);

        assertEquals("Address Line 1, Address Line 2, Town, County",
                     CrimeListHelper.formatDefendantAddress(defendantAddress),
                     "Defendant address does not match");
    }

    @Test
    void testFormatDefendantAddressWithoutPostcode() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/opaPressList.json")),
                     writer, Charset.defaultCharset()
        );
        JsonNode input = OBJECT_MAPPER.readTree(writer.toString());

        JsonNode defendantAddress = input.get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0)
            .get(SITTINGS).get(0)
            .get(HEARING).get(0)
            .get(CASE).get(0)
            .get(PARTY).get(0)
            .get(INDIVIDUAL_DETAILS)
            .get(ADDRESS);

        assertEquals("Address Line 1, Address Line 2, Town, County",
                     CrimeListHelper.formatDefendantAddressWithoutPostcode(defendantAddress),
                     "Defendant address does not match without postcode");
    }
}

