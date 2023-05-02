package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
class FamilyCauseListFileConverterTest {
    @Autowired
    FamilyDailyCauseListFileConverter familyDailyCauseListConverter;

    private static final String HEADER_TEXT = "Incorrect header text";
    private static final String PROVENANCE = "provenance";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Map<String, String> METADATA = Map.of(
        "contentDate", Instant.now().toString(),
        PROVENANCE, PROVENANCE,
        "locationName", "location",
        "language", "ENGLISH",
        "listType", "FAMILY_DAILY_CAUSE_LIST"
    );

    @Test
    void testFamilyCauseListTemplate() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/familyDailyCauseList.json")) {
            language = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        JsonNode inputJson = getInputJson();
        String outputHtml = familyDailyCauseListConverter.convert(inputJson, METADATA, language);

        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Family Daily Cause List");

        assertThat(document.getElementsByClass("govuk-heading-l")
            .get(0).text())
            .as(HEADER_TEXT).isEqualTo("Family Daily Cause List for location");

        assertThat(document.getElementsByClass("govuk-body")
                       .get(2).text())
            .as(HEADER_TEXT).contains("Last updated 21 July 2022");
    }


    @Test
    void testFamilyCauseListTemplateWelsh() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/cy/familyDailyCauseList.json")) {
            language = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        JsonNode inputJson = getInputJson();
        String outputHtml = familyDailyCauseListConverter.convert(inputJson, METADATA, language);

        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("No html found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Rhestr Ddyddiol o Achosion Teulu");

        assertThat(document.getElementsByClass("govuk-heading-l")
                       .get(0).text())
            .as(HEADER_TEXT).isEqualTo("Rhestr Ddyddiol o Achosion Teulu gyfer location");

        assertThat(document.getElementsByClass("govuk-body")
                       .get(2).text())
            .as(HEADER_TEXT).contains("Diweddarwyd ddiwethaf 21 July 2022 yn 3:01pm");
    }

    @Test
    void testTableContents() throws IOException {
        Map<String, Object> language;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream("templates/languages/en/civilAndFamilyDailyCauseList.json")) {
            language = OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
        JsonNode inputJson = getInputJson();
        String result = familyDailyCauseListConverter.convert(inputJson, METADATA, language);

        Document doc = Jsoup.parse(result);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(doc.getElementsByTag("th"))
            .as("Incorrect table headers")
            .hasSize(27)
            .extracting(Element::text)
            .startsWith("Time",
                        "Case Ref",
                        "Case Name",
                        "Case Type",
                        "Hearing Type",
                        "Location",
                        "Duration",
                        "Applicant/Petitioner",
                        "Respondent"
            );

        softly.assertThat(doc.getElementsByTag("td"))
            .as("Incorrect table contents")
            .hasSize(45)
            .extracting(Element::text)
            .contains(
                "Surname, Legal Advisor: Mr Individual Forenames Individual Middlename Individual Surname",
                "Surname",
                "Applicant org name, Legal Advisor: Applicant rep org name",
                "Respondent org name, Legal Advisor: Respondent rep org name"
            );

        softly.assertAll();
    }

    private JsonNode getInputJson() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/",
                                                    "familyDailyCauseList.json")), writer,
                     Charset.defaultCharset()
        );

        return OBJECT_MAPPER.readTree(writer.toString());
    }
}
