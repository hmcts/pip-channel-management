package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
@ActiveProfiles("test")
class CrownFirmListSummaryConverterTest {
    @Autowired
    CrownFirmListSummaryConverter crownFirmList;

    @Test
    void testCrownDailyListTemplate() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
                         "src/test/resources/mocks/",
                         "crownFirmList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        String emailOutput = crownFirmList.convert(payload);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(emailOutput)
            .as("incorrect sitting at found")
            .contains("9am");

        softly.assertThat(emailOutput)
            .as("incorrect case reference found")
            .contains("I4Y416QE");

        softly.assertThat(emailOutput)
            .as("incorrect defendant found")
            .contains("Cora, Mckinley");

        softly.assertThat(emailOutput)
            .as("incorrect hearing type found")
            .contains("Directions");

        softly.assertThat(emailOutput)
            .as("incorrect duration found")
            .contains("8 hours [4 of 5]");

        softly.assertThat(emailOutput)
            .as("incorrect representative found")
            .contains("Breakfast Daniel");

        softly.assertThat(emailOutput)
            .as("incorrect prosecuting authority found")
            .contains("Queen");

        softly.assertThat(emailOutput)
            .as("incorrect linked cases found")
            .contains("YRYCTRR3");

        softly.assertThat(emailOutput)
            .as("incorrect listing notes found")
            .contains("Listing details text");

        softly.assertAll();
    }
}
