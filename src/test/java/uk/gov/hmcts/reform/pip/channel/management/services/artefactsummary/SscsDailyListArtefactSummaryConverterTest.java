package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.services.ListConversionFactory;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;

@ActiveProfiles("test")
class SscsDailyListArtefactSummaryConverterTest {
    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SSCS_DAILY_LIST", "SSCS_DAILY_LIST_ADDITIONAL_HEARINGS"})
    void testSscsDailyListSummary(ListType listType) throws IOException {
        String output;
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/sscsDailyList.json")) {
            JsonNode payload = new ObjectMapper().readTree(new String(mockFile.readAllBytes()));
            output = new ListConversionFactory().getArtefactSummaryConverter(listType)
                .convert(payload);
        }

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(output.split(System.lineSeparator()))
            .as("Output line count does not match")
            .hasSize(12);

        softly.assertThat(output)
            .as("Court house does not match")
            .contains("Test court house name");

        softly.assertThat(output)
            .as("Court room does not match")
            .contains("1, Time: 12:30am");

        softly.assertThat(output)
            .as("Appellant does not match")
            .contains("Appellant: CasePartyNamePetitioner, Legal Advisor: Mr Test A CasePartyNamePetitionerRep");

        softly.assertThat(output)
            .as("Informant does not match")
            .contains("Prosecutor: Prosecutor1, Prosecutor2");

        softly.assertThat(output)
            .as("Party prosecutor does not match")
            .contains("Prosecutor: Prosecutor1, Prosecutor2");

        softly.assertThat(output)
            .as("Panel does not match")
            .contains("Panel: Test KnownAs, Test KnownAs 2");

        softly.assertThat(output)
            .as("Tribunal type does not match")
            .contains("Tribunal type: Teams, Attended");

        softly.assertAll();
    }
}
