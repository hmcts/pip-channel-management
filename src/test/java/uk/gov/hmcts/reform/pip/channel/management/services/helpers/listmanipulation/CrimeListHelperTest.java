package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleHelper;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class CrimeListHelperTest {
    private static JsonNode partyRoleJson;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PARTY_NAME_MESSAGE = "Party name do not match";

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter partyRoleWriter = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/partyManipulation.json")),
                     partyRoleWriter, Charset.defaultCharset()
        );

        partyRoleJson = OBJECT_MAPPER.readTree(partyRoleWriter.toString());
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

