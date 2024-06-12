package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.services.ListConversionFactory;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

@ActiveProfiles("test")
class IacDailyListSummaryConverterTest {

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"IAC_DAILY_LIST", "IAC_DAILY_LIST_ADDITIONAL_CASES"})
    void testIacDailyListTemplate(ListType listType) throws IOException {

        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
                         "src/test/resources/mocks/",
                         "iacDailyList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        String artefactSummary = new ListConversionFactory().getArtefactSummaryConverter(listType)
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
            .as("incorrect hearing channel found")
            .contains("VIDEO HEARING");

        softly.assertThat(artefactSummary)
            .as("incorrect individual claimant found")
            .contains("Surname");

        softly.assertThat(artefactSummary)
            .as("incorrect organisation claimant found")
            .contains("Organisation Name");

        softly.assertThat(artefactSummary)
            .as("incorrect prosecuting authority found")
            .contains("Authority Surname");

        softly.assertAll();
    }
}
