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
class CrownDailyListFileConverterTest {
    @Autowired
    CrownDailyListFileConverter crownDailyListConverter;

    private static final String HEADER_TEXT = "Incorrect header text";
    private static final String PROVENANCE = "provenance";

    @Test
    void testCrownDailyListTemplate() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/crownDailyList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "crownDailyList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "location",
                                                 "language", "ENGLISH",
                                                 "listType", "CROWN_DAILY_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = crownDailyListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Crown Daily List");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT).contains("Daily List");

        assertThat(document.getElementsByClass("govuk-body")
                       .get(2).text())
            .as(HEADER_TEXT).contains("Draft: Version");
    }

    @Test
    void testCrownDailyListTemplateWelsh() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/crownDailyList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "crownDailyList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "location",
                                                 "language", "WELSH",
                                                 "listType", "CROWN_DAILY_LIST"
        );

        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = crownDailyListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Crown Rhestr Ddyddiol");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT).contains("Rhestr Ddyddiol");

        assertThat(document.getElementsByClass("govuk-body")
                       .get(2).text())
            .as(HEADER_TEXT).contains("Drafft:Fersiwn");

    }
}
