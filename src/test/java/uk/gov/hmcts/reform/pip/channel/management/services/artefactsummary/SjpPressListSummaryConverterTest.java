package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.services.ListConversionFactory;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;

@ActiveProfiles("test")
class SjpPressListSummaryConverterTest {
    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SJP_PRESS_LIST", "SJP_DELTA_PRESS_LIST"})
    void testSjpPressListSummary(ListType listType) throws IOException {
        String[] outputLines;
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/sjpPressList.json")) {
            JsonNode payload = new ObjectMapper().readTree(new String(mockFile.readAllBytes()));
            String result = new ListConversionFactory().getArtefactSummaryConverter(listType)
                .convert(payload);
            outputLines = result.split(System.lineSeparator());
        }

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputLines)
            .as("Output line count does not match")
            .hasSize(17);

        softly.assertThat(outputLines[0])
            .as("Accused (using individual details) does not match")
            .isEqualTo("•Accused: Mr. Forename Middle Surname");

        softly.assertThat(outputLines[1])
            .as("Postcode does not match")
            .isEqualTo("Postcode: SE23 6FH");

        softly.assertThat(outputLines[2])
            .as("Prosecutor does not match")
            .isEqualTo("Prosecutor: Hampshire Police");

        softly.assertThat(outputLines[3])
            .as("Offence does not match")
            .isEqualTo("Offence: Sedition (Reporting restriction)");

        softly.assertThat(outputLines[4])
            .as("Accused (using organisation details) does not match")
            .isEqualTo("•Accused: Accused's organisation");

        softly.assertThat(outputLines[7])
            .as("Multiple offence line 1 does not match")
            .isEqualTo("Offence 1: Criminal Mischief (Reporting restriction)");

        softly.assertThat(outputLines[8])
            .as("Multiple offence line 2 does not match")
            .isEqualTo("Offence 2: Swampy Jorts");

        softly.assertThat(outputLines[9])
            .as("Accused (missing individualForenames and individualSurname) does not match")
            .isEqualTo("•Accused: Mrs. Middle");

        softly.assertThat(outputLines[10])
            .as("Postcode (missing address field) does not match")
            .isEqualTo("Postcode: ");

        softly.assertThat(outputLines[14])
            .as("Postcode (empty address field) does not match")
            .isEqualTo("Postcode: ");

        softly.assertAll();
    }
}
