package uk.gov.hmcts.reform.pip.channel.management.services.hearingparty;

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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST;

@ActiveProfiles("test")
class CivilAndFamilyDailyCauseListSummaryConverterTest {
    @Test
    void testCivilAndFamilyCauseListTemplate() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
            "src/test/resources/mocks/hearingparty/",
            "civilAndFamilyDailyCauseList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        String emailOutput = new ListConversionFactory().getArtefactSummaryConverter(CIVIL_AND_FAMILY_DAILY_CAUSE_LIST)
            .convert(payload);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(emailOutput)
            .as("incorrect party name found")
            .contains("This is a case name [2 of 3]");

        softly.assertThat(emailOutput)
            .as("incorrect case ID found")
            .contains("12341234");

        softly.assertThat(emailOutput)
            .as("incorrect hearing found")
            .contains("Directions");

        softly.assertThat(emailOutput)
            .as("incorrect location found")
            .contains("Teams, Attended");

        softly.assertThat(emailOutput)
            .as("incorrect duration found")
            .contains("1 hour 25 mins");

        softly.assertThat(emailOutput)
            .as("incorrect court room + judge found")
            .contains("This is the court room name, Before: Judge KnownAs Presiding, Judge KnownAs 2");

        softly.assertAll();
    }
}
