package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.Application;
import uk.gov.hmcts.reform.pip.channel.management.config.WebClientTestConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.services.ListConversionFactory;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@ActiveProfiles("test")
@SpringBootTest(classes = {Application.class, WebClientTestConfiguration.class})
class SjpPressListFileConverterTest {

    @Autowired
    SjpPressListFileConverter sjpPressListConverter;

    @Autowired
    private ListConversionFactory listConversionFactory;

    private JsonNode getInput(String resourcePath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            return new ObjectMapper().readTree(inputRaw);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SJP_PRESS_LIST", "SJP_DELTA_PRESS_LIST"})
    void testSjpPressListTemplate(ListType listType) throws IOException {
        Map<String, Object> language = TestUtils.getLanguageResources(listType, "en");
        Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                                 "provenance", "provenance",
                                                 "locationName", "location",
                                                 "language", "ENGLISH",
                                                 "listType", listType.name()
        );

        String outputHtml =  listConversionFactory.getFileConverter(listType)
            .convert(getInput("/mocks/sjpPressList.json"), metadataMap, language);
        Document document = Jsoup.parse(outputHtml);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(outputHtml).as("No html found").isNotEmpty();

        String expectedTitle = listType.equals(ListType.SJP_PRESS_LIST)
            ? "Single Justice Procedure - Press List (Full list)"
            : "Single Justice Procedure - Press List (New cases)";

        softly.assertThat(document.title())
            .as("incorrect title found.")
            .isEqualTo(expectedTitle + " " + metadataMap.get("contentDate"));

        softly.assertThat(document.getElementsByClass("mainHeaderText")
                       .select(".mainHeaderText > h1:nth-child(1)").text())
            .as("incorrect header text")
            .isEqualTo(expectedTitle);

        softly.assertThat(document.select(
                "div.pageSeparatedCase:nth-child(1) > table > tbody > tr:nth-child(2) > td").text())
            .as("Date of birth and age does not match")
            .isEqualTo("25/07/1985 (36)");

        softly.assertThat(document.select(
                "div.pageSeparatedCase:nth-child(1) > table > tbody > tr:nth-child(3) > td").text())
            .as("Address line 1 does not match")
            .isEqualTo("72 Guild Street");

        softly.assertThat(document.select(
                "div.pageSeparatedCase:nth-child(1) > table > tbody > tr:nth-child(4) > td").text())
            .as("Address line 2 does not match")
            .isEqualTo("London");

        softly.assertThat(document.select(
                "div.pageSeparatedCase:nth-child(1) > table > tbody > tr:nth-child(5) > td").text())
            .as("Address line 3 does not match")
            .isEqualTo("SE23 6FH");

        softly.assertThat(document.select(
                "div.pageSeparatedCase:nth-child(2) > table > tbody > tr:nth-child(3) > td").text())
            .as("Organisation address line 1 does not match")
            .isEqualTo("2 Main Road");

        softly.assertThat(document.select(
                "div.pageSeparatedCase:nth-child(2) > table > tbody > tr:nth-child(4) > td").text())
            .as("Organisation address line 2 does not match")
            .isEqualTo("London");

        softly.assertThat(document.select(
                "div.pageSeparatedCase:nth-child(3) > table > tbody > tr:nth-child(3) > td").text())
            .as("Address line should be empty (for missing address field)")
            .isEmpty();

        softly.assertThat(document.select(
                "div.pageSeparatedCase:nth-child(4) > table > tbody > tr:nth-child(3) > td").text())
            .as("Address line should be empty (for empty address field)")
            .isEmpty();

        softly.assertThat(document.select(
            "div.pageSeparatedCase:nth-child(1) > table > tbody > tr:nth-child(6) > td").text())
            .as("incorrect prosecutor found")
            .isEqualTo("Hampshire Police");

        Elements pages = document.getElementsByClass("pageSeparatedCase");
        softly.assertThat(pages)
            .as("Incorrect number of pages")
            .hasSize(4);

        List<String> expectedOffender = List.of(
            "Mr. Forename Middle Surname",
            "Accused's organisation",
            "Mrs. Middle",
            "Surname"
        );

        AtomicInteger count = new AtomicInteger();
        pages.forEach(p -> softly.assertThat(p.text())
            .as("Incorrect offender at index " + count.get())
            .contains(expectedOffender.get(count.getAndIncrement()))
        );

        softly.assertAll();
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SJP_PRESS_LIST", "SJP_DELTA_PRESS_LIST"})
    void testSuccessfulExcelConversion(ListType listType) throws IOException {
        byte[] result = sjpPressListConverter.convertToExcel(getInput("/mocks/sjpPressList.json"), listType);

        ByteArrayInputStream file = new ByteArrayInputStream(result);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Row headingRow = sheet.getRow(0);

        String expectedSheetName = listType.equals(ListType.SJP_PRESS_LIST)
            ? "SJP Press List (Full list)"
            : "SJP Press List (New cases)";

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(sheet.getSheetName())
            .as("Sheet name does not match")
            .isEqualTo(expectedSheetName);

        softly.assertThat(headingRow.getCell(0).getStringCellValue())
            .as("Address column is different")
            .isEqualTo("Address");

        softly.assertThat(headingRow.getCell(1).getStringCellValue())
            .as("Case URN column is different")
            .isEqualTo("Case URN");

        softly.assertThat(headingRow.getCell(2).getStringCellValue())
            .as("Date of Birth column is different")
            .isEqualTo("Date of Birth");

        softly.assertThat(headingRow.getCell(3).getStringCellValue())
            .as("Defendant Name column is different")
            .isEqualTo("Defendant Name");

        softly.assertThat(headingRow.getCell(4).getStringCellValue())
            .as("Offence 1 Press Restriction Requested column is different")
            .isEqualTo("Offence 1 Press Restriction Requested");

        softly.assertThat(headingRow.getCell(5).getStringCellValue())
            .as("Offence 1 Title column is different")
            .isEqualTo("Offence 1 Title");

        softly.assertThat(headingRow.getCell(6).getStringCellValue())
            .as("Offence 1 Wording column is different")
            .isEqualTo("Offence 1 Wording");

        softly.assertThat(headingRow.getCell(7).getStringCellValue())
            .as("Offence 2 Press Restriction Requested column is different")
            .isEqualTo("Offence 2 Press Restriction Requested");

        softly.assertThat(headingRow.getCell(8).getStringCellValue())
            .as("Offence 2 Title column is different")
            .isEqualTo("Offence 2 Title");

        softly.assertThat(headingRow.getCell(9).getStringCellValue())
            .as("Offence 2 Wording column is different")
            .isEqualTo("Offence 2 Wording");

        softly.assertThat(headingRow.getCell(10).getStringCellValue())
            .as("Prosecutor Name column is different")
            .isEqualTo("Prosecutor Name");

        softly.assertAll();
    }
}
