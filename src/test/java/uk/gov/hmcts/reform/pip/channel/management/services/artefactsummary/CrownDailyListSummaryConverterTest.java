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
import java.util.regex.Pattern;

@SpringBootTest
@ActiveProfiles("test")
class CrownDailyListSummaryConverterTest {
    @Autowired
    CrownDailyListSummaryConverter crownDailyList;

    @Test
    void testCrownDailyListTemplate() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
            "src/test/resources/mocks/",
            "crownDailyList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        String emailOutput = crownDailyList.convert(payload);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(emailOutput)
            .as("incorrect sitting at found")
            .contains("10:40am");

        softly.assertThat(emailOutput)
            .as("incorrect case reference found")
            .contains("12345678");

        softly.assertThat(emailOutput)
            .as("incorrect defendant found")
            .contains("Surname 1, Forename 1");

        softly.assertThat(emailOutput)
            .as("incorrect hearing type found")
            .contains("FHDRA1 (First Hearing and Dispute Resolution Appointment)");

        softly.assertThat(emailOutput)
            .as("incorrect duration found")
            .contains("1 hour 5 mins [2 of 3]");

        softly.assertThat(emailOutput)
            .as("incorrect prosecuting authority found")
            .contains("Pro_Auth");

        softly.assertThat(emailOutput)
            .as("incorrect reporting restriction detail found")
            .contains("This is a reporting restriction detail, This is another reporting restriction detail");

        softly.assertThat(Pattern.compile("Reporting Restriction - ").matcher(emailOutput).results().count())
            .as("incorrect number of reporting restriction detail found")
            .isEqualTo(2);

        softly.assertThat(emailOutput)
            .as("incorrect linked cases found")
            .contains("caseid111, caseid222");

        softly.assertThat(emailOutput)
            .as("incorrect listing notes found")
            .contains("Listing details text");

        softly.assertAll();
    }
}
