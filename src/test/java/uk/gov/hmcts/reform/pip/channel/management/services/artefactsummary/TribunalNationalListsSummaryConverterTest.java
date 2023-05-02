package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.services.ListConversionFactory;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;

@ActiveProfiles("test")
class TribunalNationalListsSummaryConverterTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testPrimaryHealthListTemplate() throws IOException {
        String output;
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/primaryHealthList.json")) {
            JsonNode payload = MAPPER.readTree(new String(mockFile.readAllBytes()));
            output = new ListConversionFactory().getArtefactSummaryConverter(ListType.PRIMARY_HEALTH_LIST)
                .convert(payload);
        }

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(output.split(System.lineSeparator()))
            .as("Incorrect output lines")
            .hasSize(15);

        softly.assertThat(output)
            .as("Incorrect hearing date")
            .contains("Hearing Date: 05 October");

        softly.assertThat(output)
            .as("Incorrect case name")
            .contains("Case Name: A Vs B");

        softly.assertThat(output)
            .as("Incorrect duration")
            .contains("Duration: 1 day [1 of 2]");

        softly.assertThat(output)
            .as("Incorrect location found")
            .contains("Hearing Type: Remote - Teams");

        softly.assertThat(output)
            .as("Incorrect venue")
            .contains("Venue: The Court House, Court Street, SK4 5LE");

        softly.assertAll();
    }

    @Test
    void testCareStandardListTemplate() throws IOException {
        String output;
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/careStandardsList.json")) {
            JsonNode payload = MAPPER.readTree(new String(mockFile.readAllBytes()));
            output = new ListConversionFactory().getArtefactSummaryConverter(ListType.CARE_STANDARDS_LIST)
                .convert(payload);
        }

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(output.split(System.lineSeparator()))
            .as("Incorrect output lines")
            .hasSize(15);

        softly.assertThat(output)
            .as("Incorrect hearing date")
            .contains("Hearing Date: 05 October");

        softly.assertThat(output)
            .as("Incorrect case name")
            .contains("Case Name: A Vs B");

        softly.assertThat(output)
            .as("Incorrect duration")
            .contains("Duration: 1 day [1 of 2]");

        softly.assertThat(output)
            .as("Incorrect location found")
            .contains("Hearing Type: Remote - Teams");

        softly.assertThat(output)
            .as("Incorrect venue")
            .contains("Venue: The Court House, Court Street, SK4 5LE");

        softly.assertAll();
    }
}
