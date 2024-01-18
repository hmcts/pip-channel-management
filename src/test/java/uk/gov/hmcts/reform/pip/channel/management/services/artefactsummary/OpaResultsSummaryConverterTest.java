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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.OPA_RESULTS;

@ActiveProfiles("test")
class OpaResultsSummaryConverterTest {
    @Test
    void testOpaResultsTemplate() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/opaResults.json")), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        String summary = new ListConversionFactory().getArtefactSummaryConverter(OPA_RESULTS)
            .convert(payload);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(summary.split(System.lineSeparator()))
            .as("Incorrect output lines")
            .hasSize(52);

        softly.assertThat(summary)
            .as("Incorrect defendant name")
            .contains("Defendant Name - Organisation name");

        softly.assertThat(summary)
            .as("Incorrect case URN")
            .contains("Case Ref / URN - URN456");

        softly.assertThat(summary)
            .as("Incorrect offence title")
            .contains("Offence 1 Title - Offence title 2A");

        softly.assertThat(summary)
            .as("Incorrect offence section")
            .contains("Offence 1 Section - Offence section 2A");

        softly.assertThat(summary)
            .as("Incorrect offence decision date")
            .contains("Offence 1 Decision Date - 07 January 2024");

        softly.assertThat(summary)
            .as("Incorrect offence allocation decision")
            .contains("Offence 1 Allocation Decision - Decision detail 2A");

        softly.assertThat(summary)
            .as("Incorrect offence bail status")
            .contains("Offence 1 Bail Status - Unconditional bail");

        softly.assertThat(summary)
            .as("Incorrect offence next hearing date")
            .contains("Offence 1 Next Hearing Date - 10 February 2024");

        softly.assertThat(summary)
            .as("Incorrect offence next hearing location")
            .contains("Offence 1 Next Hearing Location - Hearing location 2");

        softly.assertThat(summary)
            .as("Incorrect offence reporting restrictions")
            .contains("Offence 1 Reporting Restrictions - Reporting restriction detail 2, "
                          + "Reporting restriction detail 3");

        softly.assertAll();
    }
}
