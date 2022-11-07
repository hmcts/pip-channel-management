package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.ListType;

import java.io.IOException;
import java.io.InputStream;

@ActiveProfiles("test")
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class CrownWarnedListSummaryConverterTest {
    @Test
    void testCrownWarnedListSummary() throws IOException {
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/crownWarnedList.json")) {
            String output = ListType.CROWN_WARNED_LIST
                .getArtefactSummaryConverter()
                .convert(new String(mockFile.readAllBytes()));

            SoftAssertions softly = new SoftAssertions();

            softly.assertThat(output.split(System.lineSeparator()))
                .as("Output line count does not match")
                .hasSize(84);

            softly.assertThat(output)
                .as("Case reference does not match")
                .contains("Case Reference: 12345678");

            softly.assertThat(output)
                .as("Defendant does not match")
                .contains("Defendant Name(s): Kelly, Smith");

            softly.assertThat(output)
                .as("Hearing date does not match")
                .contains("Fixed For: 27/07/2022");

            softly.assertThat(output)
                .as("Defendant representative does not match")
                .contains("Represented By: FirstGroup LLP");

            softly.assertThat(output)
                .as("prosecuting authority does not match")
                .contains("Prosecuting Authority: CPS");

            softly.assertThat(output)
                .as("Linked cases does not match")
                .contains("Linked Cases: 123456, 123457");

            softly.assertThat(output)
                .as("Listing notes does not match")
                .contains("Listing Notes: Note 1");

            softly.assertAll();
        }
    }
}
