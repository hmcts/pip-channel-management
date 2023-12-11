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
class MagistratesStandardListSummaryConverterTest {
    @Autowired
    MagistratesStandardListSummaryConverter magistratesStandardList;

    @Test
    void testMagistratesStandardListTemplate() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
                         "src/test/resources/mocks/",
                         "magistratesStandardList.json"
                     )), writer,
                     Charset.defaultCharset()
        );

        JsonNode payload = new ObjectMapper().readTree(writer.toString());
        String emailOutput = magistratesStandardList.convert(payload);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(emailOutput)
            .as("incorrect defendant found")
            .contains("Defendant Name - Surname1, Forename1 (male)");

        softly.assertThat(emailOutput)
            .as("incorrect sitting at found")
            .contains("Sitting at - 1:30pm for 2 hours 30 mins [2 of 3]");

        softly.assertThat(emailOutput)
            .as("incorrect date of birth and age found")
            .contains("DOB and Age - 01/01/1983 Age: 39");

        softly.assertThat(emailOutput)
            .as("incorrect defendant address found")
            .contains("Defendant Address - Address Line 1, Address Line 2, Month A, County A, AA1 AA1");

        softly.assertThat(emailOutput)
            .as("incorrect prosecuting authority found")
            .contains("Prosecuting Authority - Test1234");

        softly.assertThat(emailOutput)
            .as("incorrect hearing number found")
            .contains("Hearing Number - 12");

        softly.assertThat(emailOutput)
            .as("incorrect attendance method found")
            .contains("Attendance Method - VIDEO HEARING");

        softly.assertThat(emailOutput)
            .as("incorrect case reference found")
            .contains("Case Ref - 45684548");

        softly.assertThat(emailOutput)
            .as("incorrect hearing type found")
            .contains("Hearing of Type - mda");

        softly.assertThat(emailOutput)
            .as("incorrect panel found")
            .contains("Panel - ADULT");

        softly.assertThat(emailOutput)
            .as("incorrect offence title found")
            .contains("Offence 1 Title - drink driving");

        softly.assertThat(emailOutput)
            .as("incorrect Plea")
            .contains("Offence 1 Plea - NOT_GUILTY");

        softly.assertThat(emailOutput)
            .as("incorrect convicted on found")
            .contains("Offence 1 Convicted on - 13/12/2023");

        softly.assertThat(emailOutput)
            .as("incorrect adjourned from found")
            .contains("Offence 1 Adjourned from - 13/12/2023");

        softly.assertThat(emailOutput)
            .as("incorrect offence details found")
            .contains("Offence 1 Details - driving whilst under the influence of alcohol");

        softly.assertAll();
    }
}
