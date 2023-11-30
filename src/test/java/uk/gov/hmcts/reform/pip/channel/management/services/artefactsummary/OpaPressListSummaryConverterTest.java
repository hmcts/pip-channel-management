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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.OPA_PRESS_LIST;

@ActiveProfiles("test")
class OpaPressListSummaryConverterTest {

    @Test
    void testOpaPressListTemplate() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/opaPressList.json")), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        String artefactSummary = new ListConversionFactory().getArtefactSummaryConverter(OPA_PRESS_LIST)
            .convert(payload);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(artefactSummary.split(System.lineSeparator()))
            .as("Incorrect output lines")
                .hasSize(105);

        softly.assertThat(artefactSummary)
            .as("incorrect address found")
            .contains("Address - Address Line 1, Address Line 2, Town, County");

        softly.assertThat(artefactSummary)
            .as("incorrect postcode found")
            .contains("Postcode - BB1 1BB");

        softly.assertThat(artefactSummary)
            .as("incorrect dob found")
            .contains("DOB - 01/01/1985");

        softly.assertThat(artefactSummary)
            .as("incorrect case ref found")
            .contains("Case Ref / URN - URN8888");

        softly.assertThat(artefactSummary)
            .as("incorrect offence 1 title found")
            .contains("Offence 1 Title - Offence title 2");

        softly.assertThat(artefactSummary)
            .as("incorrect offence 1 reporting restriction found")
            .contains("Offence 1 Reporting Restriction - Offence reporting restriction detail 1");

        softly.assertThat(artefactSummary)
            .as("incorrect offence 1 title found")
            .contains("Offence 2 Title - Offence title 4");

        softly.assertThat(artefactSummary)
            .as("incorrect offence 1 reporting restriction found")
            .contains("Offence 2 Reporting Restriction - Offence reporting restriction detail 2");

        softly.assertThat(artefactSummary)
            .as("incorrect case reporting restriction found")
            .contains("Reporting Restriction - Case reporting Restriction detail line 1, "
                          + "Case reporting restriction detail line 2");

        softly.assertThat(artefactSummary)
            .as("incorrect prosecutor found")
            .contains("Prosecutor - Prosecuting authority ref");

        softly.assertAll();
    }
}
