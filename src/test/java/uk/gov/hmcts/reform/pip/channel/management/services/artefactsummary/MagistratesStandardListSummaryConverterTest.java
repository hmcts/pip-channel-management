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

        String emailOutput = magistratesStandardList.convert(writer.toString());

        assertThat(emailOutput)
            .as("incorrect sitting at found")
            .contains("2:30pm");

        assertThat(emailOutput)
            .as("incorrect case reference found")
            .contains("45684548");

        assertThat(emailOutput)
            .as("incorrect defendant found")
            .contains("Surname1, John Smith1");

        assertThat(emailOutput)
            .as("incorrect hearing type found")
            .contains("mda");

        assertThat(emailOutput)
            .as("incorrect duration found")
            .contains("2 hours 30 mins [2 of 3]");


        assertThat(emailOutput)
            .as("incorrect date of birth found")
            .contains("01/01/1983");

        assertThat(emailOutput)
            .as("incorrect defendant address found")
            .contains("Address Line 1, Address Line 2, Month A, County A, AA1 AA1");

        assertThat(emailOutput)
            .as("incorrect Prosecuting Authority found")
            .contains("Test1234");

        assertThat(emailOutput)
            .as("incorrect Hearing Number found")
            .contains("12");

        assertThat(emailOutput)
            .as("incorrect Attendance Method found")
            .contains("VIDEO HEARING");

        assertThat(emailOutput)
            .as("incorrect Panel found")
            .contains("ADULT");

        assertThat(emailOutput)
            .as("incorrect offence found")
            .contains("1. drink driving");

        assertThat(emailOutput)
            .as("incorrect Plea")
            .contains("NOT_GUILTY");

        assertThat(emailOutput)
            .as("incorrect Convicted on found")
            .contains("14/09/2016");

        assertThat(emailOutput)
            .as("incorrect Adjourned from found")
            .contains("14/09/2016 - For the trial");

        assertThat(emailOutput)
            .as("incorrect offence details found")
            .contains("driving whilst under the influence of alcohol");
    }
}
