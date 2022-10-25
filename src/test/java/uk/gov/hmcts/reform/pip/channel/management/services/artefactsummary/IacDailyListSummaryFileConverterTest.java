package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.ListType;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SuppressWarnings({"PMD.LawOfDemeter"})
class IacDailyListSummaryFileConverterTest {

    @Test
    void testIacDailyListTemplate() throws IOException {

        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
                         "src/test/resources/mocks/",
                         "iacDailyList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        String artefactSummary = ListType.IAC_DAILY_LIST.getArtefactSummaryConverter().convert(writer.toString());

        assertThat(artefactSummary.split(System.lineSeparator()))
            .as("Incorrect output lines")
                .hasSize(21);

        assertThat(artefactSummary)
            .as("incorrect start time found")
            .contains("9:00pm");

        assertThat(artefactSummary)
            .as("incorrect case ref found")
            .contains("12341234");

        assertThat(artefactSummary)
            .as("incorrect hearing channel found")
            .contains("Teams, Attended");

        assertThat(artefactSummary)
            .as("incorrect claimant found")
            .contains("Surname");

        assertThat(artefactSummary)
            .as("incorrect prosecuting authority found")
            .contains("Authority Surname");
    }

}
