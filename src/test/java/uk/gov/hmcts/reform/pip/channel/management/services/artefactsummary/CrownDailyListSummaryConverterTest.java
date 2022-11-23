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

        String emailOutput = crownDailyList.convert(writer.toString());

        assertThat(emailOutput)
            .as("incorrect sitting at found")
            .contains("10:40am");

        assertThat(emailOutput)
            .as("incorrect case reference found")
            .contains("12345678");

        assertThat(emailOutput)
            .as("incorrect defendant found")
            .contains("Defendant_SN, Defendant_FN");

        assertThat(emailOutput)
            .as("incorrect hearing type found")
            .contains("FHDRA1 (First Hearing and Dispute Resolution Appointment)");

        assertThat(emailOutput)
            .as("incorrect duration found")
            .contains("1 hour 5 mins [2 of 3]");

        assertThat(emailOutput)
            .as("incorrect prosecuting authority found")
            .contains("Pro_Auth");

        assertThat(emailOutput)
            .as("incorrect linked cases found")
            .contains("caseid111, caseid222");

        assertThat(emailOutput)
            .as("incorrect listing notes found")
            .contains("Listing details text");
    }
}
