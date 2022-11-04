package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.ListType;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class TribunalNationalListsSummaryConverterTest {
    @Test
    void testPrimaryHealthListTemplate() throws IOException {
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/primaryHealthList.json")) {
            String output =
                ListType.PRIMARY_HEALTH_LIST.getArtefactSummaryConverter().convert(new String(mockFile.readAllBytes()));

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(output.split(System.lineSeparator()))
                .as("Incorrect output lines")
                .hasSize(15);

            softly.assertThat(output)
                .as("Incorrect hearing date")
                .contains("Hearing Date: 05 October");

            assertThat(output)
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

    @Test
    void testCareStandardListTemplate() throws IOException {
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/careStandardsList.json")) {
            String output =
                ListType.CARE_STANDARDS_LIST.getArtefactSummaryConverter().convert(new String(mockFile.readAllBytes()));

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(output.split(System.lineSeparator()))
                .as("Incorrect output lines")
                .hasSize(15);

            softly.assertThat(output)
                .as("Incorrect hearing date")
                .contains("Hearing Date: 05 October");

            assertThat(output)
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
}
