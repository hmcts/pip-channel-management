package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.Application;
import uk.gov.hmcts.reform.pip.channel.management.config.WebClientConfigurationTest;

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
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(classes = {Application.class, WebClientConfigurationTest.class})
class SscsDailyListFileConverterTest {

    @Autowired
    SscsDailyListFileConverter sscsDailyListConverter;

    public static final String PROVENANCE = "provenance";
    public static final String CONTENT_DATE = "contentDate";

    @Test
    void testSscsDailyList() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/sscsDailyList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/", "sscsDailyList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of(CONTENT_DATE, Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "Livingston",
                                                 "language", "ENGLISH"
        );
        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = sscsDailyListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("no HTML found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("SSCS Daily List for Livingston - "
                           + metadataMap.get(CONTENT_DATE));

        assertThat(document.getElementsByClass("mainHeaderText")
                       .select(".mainHeaderText > h1:nth-child(1)").text())
            .as("incorrect header text").isEqualTo("Social Security and Child Support");

        assertThat(document.getElementsByTag("h2").get(3).text())
            .as("Header seems to be missing.")
            .isEqualTo("Slough County Court");

        assertThat(document.getElementsByTag("p"))
            .as("data is missing")
            .hasSize(9)
            .extracting(Element::text)
            .containsSequence("Thank you for reading this document thoroughly.");


    }

    @Test
    void testSscsDailyListWelsh() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/sscsDailyList.json")) {
            language = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/", "sscsDailyList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of(CONTENT_DATE, Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "Livingston",
                                                 "language", "ENGLISH"
        );
        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());
        String outputHtml = sscsDailyListConverter.convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("no HTML found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Rhestr Ddyddiol SSCS ar gyfer Livingston - "
                           + metadataMap.get(CONTENT_DATE));

        assertThat(document.getElementsByClass("mainHeaderText")
                       .select(".mainHeaderText > h1:nth-child(1)").text())
            .as("incorrect header text").isEqualTo("Nawdd Cymdeithasol a Chynnal Plant");

        assertThat(document.getElementsByTag("h2").get(3).text())
            .as("Header seems to be missing.")
            .isEqualTo("Slough County Court");

        assertThat(document.getElementsByTag("p"))
            .as("data is missing")
            .hasSize(9)
            .extracting(Element::text)
            .containsSequence("Ffynhonnell y Data: provenance");
    }

    @Test
    void testConvertToExcelReturnsDefault() throws IOException {
        StringWriter writer = new StringWriter();
        JsonNode inputJson = new ObjectMapper().readTree(writer.toString());

        assertEquals(0, sscsDailyListConverter.convertToExcel(inputJson).length,
                     "byte array wasn't empty"
        );
    }

}