package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.Application;
import uk.gov.hmcts.reform.pip.channel.management.config.WebClientTestConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.services.ListConversionFactory;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(classes = {Application.class, WebClientTestConfiguration.class})
class SscsDailyListFileConverterTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String LANGUAGE_FILE_PATH = "templates/languages/";

    private static final String PROVENANCE = "provenance";
    private static final String CONTENT_DATE = "contentDate";

    @Autowired
    private ListConversionFactory listConversionFactory;

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SSCS_DAILY_LIST", "SSCS_DAILY_LIST_ADDITIONAL_HEARINGS"})
    void testSscsDailyList(ListType listType) throws IOException {
        Map<String, Object> language = getLanguageResources(listType, "en");
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/", "sscsDailyList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of(CONTENT_DATE, Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "Livingston",
                                                 "language", "ENGLISH"
        );
        JsonNode inputJson = OBJECT_MAPPER.readTree(writer.toString());
        String outputHtml = listConversionFactory.getFileConverter(listType)
            .convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("no HTML found").isNotEmpty();

        String expectedTitle = "";
        if (listType.equals(ListType.SSCS_DAILY_LIST)) {
            expectedTitle = "SSCS Daily List for Livingston - ";
        } else if (listType.equals(ListType.SSCS_DAILY_LIST_ADDITIONAL_HEARINGS)) {
            expectedTitle = "SSCS Daily List - Additional Hearings for Livingston - ";
        }
        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo(expectedTitle
                           + metadataMap.get(CONTENT_DATE));

        assertThat(document.getElementsByClass("mainHeaderText")
                       .select(".mainHeaderText > h1:nth-child(1)").text())
            .as("incorrect header text")
            .isEqualTo("Social Security and Child Support");

        assertThat(document.getElementsByClass("govuk-warning-text__text").get(0).text())
            .as("incorrect warning text")
            .isEqualTo("Please note: There may be 2 hearing lists available for this date. Please make sure "
                           + "you look at both lists to see all hearings happening on this date for this location.");

        assertThat(document.getElementsByTag("h2").get(3).text())
            .as("Header seems to be missing.")
            .isEqualTo("Slough County Court");

        assertThat(document.getElementsByTag("p"))
            .as("data is missing")
            .hasSize(8)
            .extracting(Element::text)
            .containsSequence("Thank you for reading this document thoroughly.");

        assertThat(document.getElementsByTag("td"))
            .as("Incorrect appellant")
            .extracting(Element::text)
            .contains("Lovekesh, Legal Advisor: Mr Sausage Alpha Foxtrot");
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SSCS_DAILY_LIST", "SSCS_DAILY_LIST_ADDITIONAL_HEARINGS"})
    void testSscsDailyListWelsh(ListType listType) throws IOException {
        Map<String, Object> language = getLanguageResources(listType, "cy");
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/", "sscsDailyList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of(CONTENT_DATE, Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "Livingston",
                                                 "language", "WELSH"
        );
        JsonNode inputJson = OBJECT_MAPPER.readTree(writer.toString());
        String outputHtml = listConversionFactory.getFileConverter(listType)
            .convert(inputJson, metadataMap, language);
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
            .hasSize(8)
            .extracting(Element::text)
            .containsSequence("Ffynhonnell y Data: provenance");

        assertThat(document.getElementsByTag("td"))
            .as("Incorrect appellant")
            .extracting(Element::text)
            .contains("Lovekesh, Cynghorydd Cyfreithiol: Mr Sausage Alpha Foxtrot");
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SSCS_DAILY_LIST", "SSCS_DAILY_LIST_ADDITIONAL_HEARINGS"})
    void testConvertToExcelReturnsDefault(ListType listType) throws IOException {
        StringWriter writer = new StringWriter();
        JsonNode inputJson = OBJECT_MAPPER.readTree(writer.toString());

        assertEquals(0, listConversionFactory.getFileConverter(listType).convertToExcel(inputJson).length,
                     "byte array wasn't empty"
        );
    }

    private Map<String, Object> getLanguageResources(ListType listType, String language) throws IOException {
        Map<String, Object> languageResources = readResources(listType, language);

        if (listType == ListType.SSCS_DAILY_LIST_ADDITIONAL_HEARINGS) {
            Map<String, Object> parentLanguageResources = readResources(listType.getParentListType(), language);
            parentLanguageResources.putAll(languageResources);
            return parentLanguageResources;
        }
        return languageResources;
    }

    private Map<String, Object> readResources(ListType listType, String language) throws IOException {
        String languageFileName = UPPER_UNDERSCORE.to(LOWER_CAMEL, listType.name());

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(LANGUAGE_FILE_PATH + language + "/" + languageFileName + ".json")) {
            return OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
    }
}
