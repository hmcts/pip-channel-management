package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.ListType;

import java.io.IOException;
import java.io.InputStream;

@ActiveProfiles("test")
class SscsDailyListArtefactSummaryConverterTest {
    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SSCS_DAILY_LIST", "SSCS_DAILY_LIST_ADDITIONAL_HEARINGS"})
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void testCrownWarnedListSummary(ListType listType) throws IOException {
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/sscsDailyList.json")) {
            String output = listType
                .getArtefactSummaryConverter()
                .convert(new String(mockFile.readAllBytes()));

            SoftAssertions softly = new SoftAssertions();

            softly.assertThat(output.split(System.lineSeparator()))
                .as("Output line count does not match")
                .hasSize(40);

            softly.assertThat(output)
                .as("Court house does not match")
                .contains("Reading Crown Court");

            softly.assertThat(output)
                .as("Court room does not match")
                .contains("Court room 1, Time: 12:30am");

            softly.assertThat(output)
                .as("Appellant does not match")
                .contains("Appellant: Lovekesh");

            softly.assertThat(output)
                .as("Legal advisor does not match")
                .contains("Legal Advisor: Mr Sausage Alpha Foxtrot");

            softly.assertThat(output)
                .as("prosecutor does not match")
                .contains("Prosecutor: NVQ, SQA");

            softly.assertThat(output)
                .as("Penel does not match")
                .contains("Panel: Judge Judy, Magistrate Statham");

            softly.assertThat(output)
                .as("Tribunal type does not match")
                .contains("Tribunal type: Teams, Attended");

            softly.assertAll();
        }
    }
}
