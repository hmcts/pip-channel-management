package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.ListType;

import java.io.IOException;
import java.io.InputStream;

@ActiveProfiles("test")
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class EtDailyListSummaryConverterTest {
    @Test
    void testEtDailyListTemplate() throws IOException {

        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/etDailyList.json")) {
            String output =
                ListType.ET_DAILY_LIST.getArtefactSummaryConverter().convert(new String(mockFile.readAllBytes()));

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(output.split(System.lineSeparator()))
                .as("Incorrect output lines")
                .hasSize(40);

            softly.assertThat(output)
                .as("Incorrect start time")
                .contains("Start Time: 9:30am");

            softly.assertThat(output)
                .as("Incorrect duration")
                .contains("Duration: 2 hours");

            softly.assertThat(output)
                .as("Incorrect case number")
                .contains("Case Number: 12341234");

            softly.assertThat(output)
                .as("Incorrect claimant")
                .contains("Claimant: HRH G Anderson, Rep: Mr R Hargreaves");

            softly.assertThat(output)
                .as("Incorrect respondent")
                .contains("Respondent: Capt. S Jenkins, Rep: Dr M Naylor");

            softly.assertThat(output)
                .as("Incorrect hearing type")
                .contains("Hearing Type: This is a hearing type");

            softly.assertThat(output)
                .as("Incorrect jurisdiction")
                .contains("Jurisdiction: This is a case type");

            softly.assertThat(output)
                .as("Incorrect hearing platform")
                .contains("Hearing Platform: This is a sitting channel");

            softly.assertAll();
        }
    }
}