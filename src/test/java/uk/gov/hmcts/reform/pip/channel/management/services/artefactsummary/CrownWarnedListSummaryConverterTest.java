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
class CrownWarnedListSummaryConverterTest {
    @Test
    void testCrownWarnedListSummary() throws IOException {
        String output;
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/crownWarnedList.json")) {
            JsonNode payload = new ObjectMapper().readTree(new String(mockFile.readAllBytes()));
            output = new ListConversionFactory().getArtefactSummaryConverter(ListType.CROWN_WARNED_LIST)
                .convert(payload);
        }

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
            .contains("Represented By: Defendant rep 1");

        softly.assertThat(output)
            .as("prosecuting authority does not match")
            .contains("Prosecuting Authority: Prosecutor");

        softly.assertThat(output)
            .as("Linked cases does not match")
            .contains("Linked Cases: 123456, 123457");

        softly.assertThat(output)
            .as("Listing notes does not match")
            .contains("Listing Notes: Note 1");

        softly.assertAll();
    }
}
