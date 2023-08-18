package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CopCauseListFileConverterTest {

    @Autowired
    CopDailyCauseListFileConverter copDailyCauseListConverter;

    private static final String PROVENANCE = "provenance";
    private static final String HEADER_TEXT = "incorrect header text";

    @Test
    void testCopCauseListTemplate() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/copDailyCauseList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/copDailyCauseList.json")), writer,
                     Charset.defaultCharset()
        );

        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "location",
                                                 "language", "ENGLISH",
                                                 "listType", "COP_DAILY_CAUSE_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = copDailyCauseListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Court of Protection Daily Cause List");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT)
            .isEqualTo("In the Court of Protection Regional COP Court");

        assertThat(document.getElementsByClass("govuk-body")
                       .get(1).text())
            .as(HEADER_TEXT)
            .contains("Last updated 14 February 2022 at 10:30am");

        assertThat(document.getElementsByClass("govuk-accordion__section-heading")
                       .get(0).text())
            .as(HEADER_TEXT)
            .contains("Room 1, Before Crown Judge");

        assertThat(document.getElementsByClass("govuk-accordion__section-heading")
                       .get(3).text())
            .as(HEADER_TEXT)
            .contains("Room 2")
            .doesNotContain("Before");

    }

    @Test
    void testCopCauseListTemplateWelsh() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/copDailyCauseList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/copDailyCauseList.json")), writer,
                     Charset.defaultCharset()
        );

        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "location",
                                                 "language", "WELSH",
                                                 "listType", "COP_DAILY_CAUSE_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = copDailyCauseListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo("Rhestr Achosion Ddyddiol y Llys Gwarchod");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT)
            .isEqualTo("Yn y Llys Gwarchod Regional COP Court");

        assertThat(document.getElementsByClass("govuk-body")
                       .get(1).text())
            .as(HEADER_TEXT)
            .contains("Diweddarwyd ddiwethaf 14 February 2022 yn 10:30am");

        assertThat(document.getElementsByClass("govuk-accordion__section-heading")
                       .get(0).text())
            .as(HEADER_TEXT)
            .contains("Room 1, Cyn Crown Judge");
    }
}
