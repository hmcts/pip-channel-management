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
class MagistratesPublicListSummaryConverterTest {
    @Autowired
    MagistratesPublicListSummaryConverter magistratesPublicList;

    @Test
    void testMagistratesPublicListTemplate() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
            "src/test/resources/mocks/",
            "magistratesPublicList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        String emailOutput = magistratesPublicList.convert(payload);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(emailOutput)
            .as("incorrect sitting at found")
            .contains("10:40am");

        softly.assertThat(emailOutput)
            .as("incorrect case reference found")
            .contains("12345678");

        softly.assertThat(emailOutput)
            .as("incorrect defendant found")
            .contains("Defendant_SN, Defendant_FN");

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
            .as("incorrect listing notes found")
            .contains("Listing details text");

        softly.assertAll();
    }
}
