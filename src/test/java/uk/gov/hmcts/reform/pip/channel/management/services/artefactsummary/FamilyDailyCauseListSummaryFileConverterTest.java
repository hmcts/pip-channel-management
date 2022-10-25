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
class FamilyDailyCauseListSummaryFileConverterTest {

    @Test
    void testFamilyCauseListTemplate() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
            "src/test/resources/mocks/",
            "familyDailyCauseList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        String emailOutput = ListType.FAMILY_DAILY_CAUSE_LIST.getArtefactSummaryConverter().convert(writer.toString());

        assertThat(emailOutput)
            .as("incorrect party name found")
            .contains("This is a case name [2 of 3]");

        assertThat(emailOutput)
            .as("incorrect case ID found")
            .contains("12341234");

        assertThat(emailOutput)
            .as("incorrect hearing found")
            .contains("Directions");

        assertThat(emailOutput)
            .as("incorrect location found")
            .contains("Teams, Attended");

        assertThat(emailOutput)
            .as("incorrect duration found")
            .contains("1 hour 25 mins");

        assertThat(emailOutput)
            .as("incorrect judge found")
            .contains("This is the court room name");
    }
}
