package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.services.ListConversionFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.ET_FORTNIGHTLY_PRESS_LIST;

@SpringBootTest
@ActiveProfiles("test")
class EtFortnightlyPressListSummaryConverterTest {
    @Autowired
    ListConversionFactory listConversionFactory;

    @Test
    void testEtFortnightlyPressListTemplate() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
            "src/test/resources/mocks/",
            "etFortnightlyPressList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        String emailOutput = listConversionFactory.getArtefactSummaryConverter(ET_FORTNIGHTLY_PRESS_LIST)
            .convert(payload);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(emailOutput)
            .as("incorrect courtroom name found")
            .contains("Court 1");

        softly.assertThat(emailOutput)
            .as("incorrect start time found")
            .contains("9:30am");

        softly.assertThat(emailOutput)
            .as("incorrect duration found")
            .contains("2 hours [2 of 3]");

        softly.assertThat(emailOutput)
            .as("incorrect case number found")
            .contains("12341234");

        softly.assertThat(emailOutput)
            .as("incorrect Claimant found")
            .contains("Rep: Mr T Test Surname 2");

        softly.assertThat(emailOutput)
            .as("incorrect Respondent found")
            .contains("Capt. T Test Surname");

        softly.assertThat(emailOutput)
            .as("incorrect hearing type found")
            .contains("This is a hearing type");

        softly.assertThat(emailOutput)
            .as("incorrect Jurisdiction found")
            .contains("This is a case type");

        softly.assertThat(emailOutput)
            .as("incorrect Hearing Platform found")
            .contains("This is a sitting channel");

        softly.assertAll();
    }
}
