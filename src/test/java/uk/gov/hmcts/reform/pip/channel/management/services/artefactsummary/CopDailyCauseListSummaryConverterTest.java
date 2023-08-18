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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.COP_DAILY_CAUSE_LIST;

@ActiveProfiles("test")
class CopDailyCauseListSummaryConverterTest {

    @Test
    void testCopDailyCauseListTemplate() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
            "src/test/resources/mocks/copDailyCauseList.json"
                     )), writer, Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        String emailOutput = new ListConversionFactory().getArtefactSummaryConverter(COP_DAILY_CAUSE_LIST)
            .convert(payload);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(emailOutput)
            .as("incorrect case suppression name found")
            .contains("ThisIsACaseSuppressionName");

        softly.assertThat(emailOutput)
            .as("incorrect case ID found")
            .contains("12341234");

        softly.assertThat(emailOutput)
            .as("incorrect hearing found")
            .contains("Criminal");

        softly.assertThat(emailOutput)
            .as("incorrect location found")
            .contains("Teams, In-Person");

        softly.assertThat(emailOutput)
            .as("incorrect duration found")
            .contains("1 hour [1 of 2]");

        softly.assertThat(emailOutput)
            .as("incorrect judge found")
            .contains("Crown Judge");

        softly.assertAll();
    }

}
