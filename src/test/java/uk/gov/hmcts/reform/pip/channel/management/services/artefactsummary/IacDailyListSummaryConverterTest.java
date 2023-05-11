package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.services.ListConversionFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.IAC_DAILY_LIST;

@ActiveProfiles("test")
class IacDailyListSummaryConverterTest {

    @Test
    void testIacDailyListTemplate() throws IOException {

        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
                         "src/test/resources/mocks/",
                         "iacDailyList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        String artefactSummary = new ListConversionFactory().getArtefactSummaryConverter(IAC_DAILY_LIST)
            .convert(payload);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(artefactSummary.split(System.lineSeparator()))
            .as("Incorrect output lines")
                .hasSize(35);

        softly.assertThat(artefactSummary)
            .as("incorrect start time found")
            .contains("9:00pm");

        softly.assertThat(artefactSummary)
            .as("incorrect case ref found")
            .contains("12341234");

        softly.assertThat(artefactSummary)
            .as("incorrect hearing channel found")
            .contains("Teams, Attended");

        softly.assertThat(artefactSummary)
            .as("incorrect claimant found")
            .contains("Surname");

        softly.assertThat(artefactSummary)
            .as("incorrect prosecuting authority found")
            .contains("Authority Surname");

        softly.assertAll();
    }
}
