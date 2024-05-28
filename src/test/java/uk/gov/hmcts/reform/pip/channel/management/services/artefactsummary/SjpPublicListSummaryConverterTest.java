package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.pip.channel.management.services.ListConversionFactory;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;

class SjpPublicListSummaryConverterTest {
    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SJP_PUBLIC_LIST", "SJP_DELTA_PUBLIC_LIST"})
    void testSjpPublicListSummary(ListType listType) throws IOException {
        String[] outputLines;
        try (InputStream mockFile = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("mocks/sjpPublicList.json")) {
            JsonNode payload = new ObjectMapper().readTree(new String(mockFile.readAllBytes()));
            String result = new ListConversionFactory().getArtefactSummaryConverter(listType)
                .convert(payload);
            outputLines = result.split(System.lineSeparator());
        }

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputLines)
            .as("Output line count does not match")
            .hasSize(8);

        softly.assertThat(outputLines[0])
            .as("Accused (using individual details) does not match")
            .isEqualTo("•Defendant: A This is a surname");

        softly.assertThat(outputLines[1])
            .as("Postcode (using individual details) does not match")
            .isEqualTo("Postcode: AA1");

        softly.assertThat(outputLines[2])
            .as("Prosecutor does not match")
            .isEqualTo("Prosecutor: This is a prosecutor organisation");

        softly.assertThat(outputLines[3])
            .as("Offence does not match")
            .isEqualTo("Offence: This is an offence title, This is an offence title 2");

        softly.assertThat(outputLines[4])
            .as("Accused (using organisation details) does not match")
            .isEqualTo("•Defendant: This is an accused organisation name");

        softly.assertThat(outputLines[5])
            .as("Postcode (using organisation details) does not match")
            .isEqualTo("Postcode: A99");

        softly.assertAll();
    }
}
