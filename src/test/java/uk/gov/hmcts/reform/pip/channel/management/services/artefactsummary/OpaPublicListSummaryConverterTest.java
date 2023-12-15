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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.OPA_PUBLIC_LIST;

@ActiveProfiles("test")
class OpaPublicListSummaryConverterTest {

    @Test
    void testOpaPublicListTemplate() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/opaPublicList.json")), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        String artefactSummary = new ListConversionFactory().getArtefactSummaryConverter(OPA_PUBLIC_LIST)
            .convert(payload);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(artefactSummary.split(System.lineSeparator()))
            .as("Incorrect output lines")
            .hasSize(76);

        softly.assertThat(artefactSummary)
            .as("incorrect name found")
            .contains("Defendant - individualFirstName individualMiddleName IndividualSurname");

        softly.assertThat(artefactSummary)
            .as("incorrect organisation name found")
            .contains("Defendant - defendantOrganisationName");

        softly.assertThat(artefactSummary)
            .as("incorrect case ref found")
            .contains("Case Ref / URN - URN1234");

        softly.assertThat(artefactSummary)
            .as("incorrect offence 1 title found")
            .contains("Offence 1 Title - Offence title 2");

        softly.assertThat(artefactSummary)
            .as("incorrect offence 1 reporting restriction found")
            .contains("Offence 1 Reporting Restriction - Offence Reporting Restriction detail");

        softly.assertThat(artefactSummary)
            .as("incorrect offence 1 title found")
            .contains("Offence 2 Title - Offence title 3");

        softly.assertThat(artefactSummary)
            .as("incorrect offence 1 reporting restriction found")
            .contains("Offence 2 Reporting Restriction - Offence Reporting Restriction detail 3");

        softly.assertThat(artefactSummary)
            .as("incorrect case reporting restriction found")
            .contains("Reporting Restriction - Case Reporting Restriction detail line 1, "
                          + "Case Reporting restriction detail line 2");

        softly.assertThat(artefactSummary)
            .as("incorrect prosecutor found at informant level")
            .contains("Prosecutor - Prosecution Authority ref 1");

        softly.assertThat(artefactSummary)
            .as("incorrect prosecutor found")
            .contains("Prosecutor - organisationName");

        softly.assertAll();
    }
}
