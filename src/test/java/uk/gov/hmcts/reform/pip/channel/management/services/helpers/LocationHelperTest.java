package uk.gov.hmcts.reform.pip.channel.management.services.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
class LocationHelperTest {
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String FORMATTED_COURT_HOUSE_ADDRESS = "formattedCourtHouseAddress";
    private static final String FORMATTED_SESSION_COURT_ROOM = "formattedSessionCourtRoom";
    private static final String VENUE_ADDRESS_ERROR = "Incorrect venue address";
    private static final String COURT_ADDRESS_ERROR = "Incorrect court house address";
    private static final String DELIMITER = "|";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static JsonNode inputJson;

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/familyDailyCauseList.json")),
                     writer, Charset.defaultCharset()
        );

        inputJson = new ObjectMapper().readTree(writer.toString());
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void testFormatVenueAddress() {
        List<String> venueAddress = LocationHelper.formatVenueAddress(inputJson);

        assertThat(venueAddress)
            .as(VENUE_ADDRESS_ERROR)
            .hasSize(2)
            .containsExactly("Address Line 1", "AA1 AA1");
    }

    @Test
    void testFormatFullVenueAddress() {
        List<String> venueAddress = LocationHelper.formatFullVenueAddress(inputJson);

        assertThat(venueAddress)
            .as(VENUE_ADDRESS_ERROR)
            .hasSize(4)
            .containsExactly("Address Line 1", "Venue Town", "Venue County", "AA1 AA1");
    }

    @Test
    void testFormatCourtAddress() {
        LocationHelper.formatCourtAddress(inputJson, DELIMITER, false);
        JsonNode courtHouse = inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE);

        assertThat(courtHouse.has(FORMATTED_COURT_HOUSE_ADDRESS))
            .as(COURT_ADDRESS_ERROR)
            .isTrue();

        assertThat(courtHouse.get(FORMATTED_COURT_HOUSE_ADDRESS).asText())
            .as(COURT_ADDRESS_ERROR)
            .isEqualTo("Address Line 1|Venue Town|Venue County|AA1 AA1");
    }

    @Test
    void testFormatCourtAddressWithNewLineDelimiter() {
        LocationHelper.formatCourtAddress(inputJson, "\n", false);
        JsonNode courtHouse = inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE);

        assertThat(courtHouse.has(FORMATTED_COURT_HOUSE_ADDRESS))
            .as(COURT_ADDRESS_ERROR)
            .isTrue();

        assertThat(courtHouse.get(FORMATTED_COURT_HOUSE_ADDRESS).asText())
            .as(COURT_ADDRESS_ERROR)
            .isEqualTo("Address Line 1\nVenue Town\nVenue County\nAA1 AA1");
    }

    @Test
    void testFormatCourtAddressIncludingCourtHouseName() {
        LocationHelper.formatCourtAddress(inputJson, DELIMITER, true);
        JsonNode courtHouse = inputJson.get(COURT_LISTS).get(0).get(COURT_HOUSE);

        assertThat(courtHouse.has(FORMATTED_COURT_HOUSE_ADDRESS))
            .as(COURT_ADDRESS_ERROR)
            .isTrue();

        assertThat(courtHouse.get(FORMATTED_COURT_HOUSE_ADDRESS).asText())
            .as(COURT_ADDRESS_ERROR)
            .isEqualTo("This is the site name|Address Line 1|Venue Town|Venue County|AA1 AA1");
    }

    @Test
    void testFormatWithNoCourtAddress() {
        LocationHelper.formatCourtAddress(inputJson, DELIMITER, false);
        JsonNode courtHouse = inputJson.get(COURT_LISTS).get(1).get(COURT_HOUSE);

        assertThat(courtHouse.has(FORMATTED_COURT_HOUSE_ADDRESS))
            .as(COURT_ADDRESS_ERROR)
            .isTrue();

        assertThat(courtHouse.get(FORMATTED_COURT_HOUSE_ADDRESS).asText())
            .as(COURT_ADDRESS_ERROR)
            .isEmpty();
    }

    @Test
    void testFormatWithCourtHouseNameOnly() {
        LocationHelper.formatCourtAddress(inputJson, DELIMITER, true);
        JsonNode courtHouse = inputJson.get(COURT_LISTS).get(1).get(COURT_HOUSE);

        assertThat(courtHouse.has(FORMATTED_COURT_HOUSE_ADDRESS))
            .as(COURT_ADDRESS_ERROR)
            .isTrue();

        assertThat(courtHouse.get(FORMATTED_COURT_HOUSE_ADDRESS).asText())
            .as(COURT_ADDRESS_ERROR)
            .isEqualTo("This is the site name");
    }

    @Test
    void testFormatWithNoCourtNameAndAddressLine() {
        LocationHelper.formatCourtAddress(inputJson, DELIMITER, false);
        JsonNode courtHouse = inputJson.get(COURT_LISTS).get(2).get(COURT_HOUSE);

        assertThat(courtHouse.has(FORMATTED_COURT_HOUSE_ADDRESS))
            .as(COURT_ADDRESS_ERROR)
            .isTrue();

        assertThat(courtHouse.get(FORMATTED_COURT_HOUSE_ADDRESS).asText())
            .as(COURT_ADDRESS_ERROR)
            .isEqualTo("Address Line 1|Address Line 2|AA1 AA1");
    }

    @Test
    void testFormatWithBlankCourtNameAndAddressLine() {
        LocationHelper.formatCourtAddress(inputJson, DELIMITER, true);
        JsonNode courtHouse = inputJson.get(COURT_LISTS).get(2).get(COURT_HOUSE);

        assertThat(courtHouse.has(FORMATTED_COURT_HOUSE_ADDRESS))
            .as(COURT_ADDRESS_ERROR)
            .isTrue();

        assertThat(courtHouse.get(FORMATTED_COURT_HOUSE_ADDRESS).asText())
            .as(COURT_ADDRESS_ERROR)
            .isEqualTo("Address Line 1|Address Line 2|AA1 AA1");
    }

    @Test
    void testFormatCourtRoomNameWithNoJudiciary() {
        ObjectNode courtRoom = OBJECT_MAPPER.createObjectNode();
        ObjectNode session = OBJECT_MAPPER.createObjectNode();
        StringBuilder stringBuilder = new StringBuilder();

        courtRoom.put("courtRoomName", "This is a court room name");

        LocationHelper.formattedCourtRoomName(courtRoom, session,stringBuilder);

        assertThat(session.has(FORMATTED_SESSION_COURT_ROOM))
            .as("Formatted court room name not present")
            .isTrue();

        assertThat(session.get(FORMATTED_SESSION_COURT_ROOM).asText())
            .as("Formatted court room name not correct")
            .isEqualTo("This is a court room name");
    }

    @Test
    void testFormatCourtRoomNameWithJudiciary() {
        ObjectNode courtRoom = OBJECT_MAPPER.createObjectNode();
        ObjectNode session = OBJECT_MAPPER.createObjectNode();
        StringBuilder stringBuilder = new StringBuilder(20);

        courtRoom.put("courtRoomName", "This is a court room name");
        stringBuilder.append("This is a judiciary");

        LocationHelper.formattedCourtRoomName(courtRoom, session,stringBuilder);

        assertThat(session.has(FORMATTED_SESSION_COURT_ROOM))
            .as("Formatted court room name not present")
            .isTrue();

        assertThat(session.get(FORMATTED_SESSION_COURT_ROOM).asText())
            .as("Formatted court room name not correct")
            .isEqualTo("This is a court room name: This is a judiciary");
    }

    @Test
    void testFormatCourtRoomNameWithNoName() {
        ObjectNode courtRoom = OBJECT_MAPPER.createObjectNode();
        ObjectNode session = OBJECT_MAPPER.createObjectNode();
        StringBuilder stringBuilder = new StringBuilder(20);
        stringBuilder.append("This is a judiciary");


        LocationHelper.formattedCourtRoomName(courtRoom, session,stringBuilder);

        assertThat(session.has(FORMATTED_SESSION_COURT_ROOM))
            .as("Court Room name shown when no court room")
            .isTrue();

        assertThat(session.get(FORMATTED_SESSION_COURT_ROOM).asText())
            .as("Formatted court room name does not match judiciary")
            .isEqualTo("This is a judiciary");
    }
}
