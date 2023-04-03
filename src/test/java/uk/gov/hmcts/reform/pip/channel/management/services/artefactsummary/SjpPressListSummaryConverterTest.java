package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.services.ListConversionFactory;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;

@ActiveProfiles("test")
class SjpPressListSummaryConverterTest {
    @Test
    void testSjpPressListSummary() throws IOException {
        String[] outputLines;
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/sjpPressList.json")) {
            String result = new ListConversionFactory().getArtefactSummaryConverter(ListType.SJP_PRESS_LIST)
                .convert(new String(mockFile.readAllBytes()));
            outputLines = result.split(System.lineSeparator());
        }

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputLines)
            .as("Output line count does not match")
            .hasSize(13);

        softly.assertThat(outputLines[0])
            .as("Accused does not match")
            .isEqualTo("•Accused: Forename1 Surname1");

        softly.assertThat(outputLines[1])
            .as("Postcode does not match")
            .isEqualTo("Postcode: SE23 6FH");

        softly.assertThat(outputLines[2])
            .as("Prosecutor does not match")
            .isEqualTo("Prosecutor: Hampshire Police");

        softly.assertThat(outputLines[3])
            .as("Offence does not match")
            .isEqualTo("Offence: Sedition(Reporting restriction)");

        softly.assertThat(outputLines[7])
            .as("Multiple offence line 1 does not match")
            .isEqualTo("Offence 1: Criminal Mischief(Reporting restriction)");

        softly.assertThat(outputLines[8])
            .as("Multiple offence line 2 does not match")
            .isEqualTo("Offence 2: Swampy Jorts");

        softly.assertAll();
    }
}
