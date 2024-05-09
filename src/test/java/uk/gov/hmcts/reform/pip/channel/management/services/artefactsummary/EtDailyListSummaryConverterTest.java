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
class EtDailyListSummaryConverterTest {
    @Test
    void testEtDailyListTemplate() throws IOException {
        String output;
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/etDailyList.json")) {
            JsonNode payload = new ObjectMapper().readTree(new String(mockFile.readAllBytes()));
            output = new ListConversionFactory().getArtefactSummaryConverter(ListType.ET_DAILY_LIST)
                .convert(payload);
        }

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(output.split(System.lineSeparator()))
            .as("Incorrect output lines")
            .hasSize(35);

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
            .as("Incorrect individual claimant")
            .contains("Claimant: Mr T Test Surname");

        softly.assertThat(output)
            .as("Incorrect individual respondent")
            .contains("Capt. T Test Surname 2");

        softly.assertThat(output)
            .as("Incorrect organisation claimant")
            .contains("Claimant: Organisation Name");

        softly.assertThat(output)
            .as("Incorrect organisation respondent")
            .contains("Respondent: Organisation Name");

        softly.assertThat(output)
            .as("Incorrect hearing type")
            .contains("Hearing Type: This is a hearing type");

        softly.assertThat(output)
            .as("Incorrect hearing platform")
            .contains("Hearing Platform: This is a sitting channel");

        softly.assertAll();
    }
}
