package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.services.ListConversionFactory;

import java.io.IOException;
import java.io.InputStream;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.CIVIL_DAILY_CAUSE_LIST;

@ActiveProfiles("test")
class CivilDailyCauseListSummaryConverterTest {
    @Test
    void testCivilDailyCauseListSummary() throws IOException {
        String[] outputLines;
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/civilDailyCauseList.json")) {
            JsonNode payload = new ObjectMapper().readTree(new String(mockFile.readAllBytes()));
            String result = new ListConversionFactory().getArtefactSummaryConverter(CIVIL_DAILY_CAUSE_LIST)
                .convert(payload);
            outputLines = result.split(System.lineSeparator());
        }

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputLines)
            .as("Output line count does not match")
            .hasSize(82);

        softly.assertThat(outputLines[2])
            .as("Court room name does not match")
            .isEqualTo("Courtroom: Courtroom 1");

        softly.assertThat(outputLines[3])
            .as("Judiciary does not match")
            .isEqualTo("Judiciary:  Doctor, SSCS,  Judge Jones");

        softly.assertThat(outputLines[4])
            .as("Hearing number does not match")
            .isEqualTo("•Hearing 1");

        softly.assertThat(outputLines[5])
            .as("Case name does not match")
            .isEqualTo("Case Name: Surname v Surname");

        softly.assertThat(outputLines[6])
            .as("Case reference does not match")
            .isEqualTo("Case Reference: OX21P00298");

        softly.assertThat(outputLines[7])
            .as("Hearing type does not match")
            .isEqualTo("Hearing Type: FHDRA (First Hearing and Dispute Resolution Appointment)");

        softly.assertThat(outputLines[8])
            .as("Start time does not match")
            .isEqualTo("Start Time: 9:30am");

        softly.assertThat(outputLines[9])
            .as("Hearing channel does not match")
            .isEqualTo("Hearing Channel: Remote, Teams");

        softly.assertAll();
    }
}
