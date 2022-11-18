package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

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

        String emailOutput = crownFirmList.convert(writer.toString());

        assertThat(emailOutput)
            .as("incorrect sitting at found")
            .contains("9:00am");

        assertThat(emailOutput)
            .as("incorrect case reference found")
            .contains("I4Y416QE");

        assertThat(emailOutput)
            .as("incorrect defendant found")
            .contains("Cora, Mckinley");

        assertThat(emailOutput)
            .as("incorrect hearing type found")
            .contains("Directions");

        assertThat(emailOutput)
            .as("incorrect duration found")
            .contains("8 hours [4 of 5]");

        assertThat(emailOutput)
            .as("incorrect representative found")
            .contains("Daniel Breakfast");

        assertThat(emailOutput)
            .as("incorrect prosecuting authority found")
            .contains("Queen");

        assertThat(emailOutput)
            .as("incorrect linked cases found")
            .contains("YRYCTRR3");

        assertThat(emailOutput)
            .as("incorrect listing notes found")
            .contains("Listing details text");
    }
}
