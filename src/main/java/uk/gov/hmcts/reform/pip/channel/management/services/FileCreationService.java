package uk.gov.hmcts.reform.pip.channel.management.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import uk.gov.hmcts.reform.pip.channel.management.config.ThymeleafConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Artefact;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.ListType;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Location;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.Converter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for functionality related to building PDF and xlsx files from JSON input. Thymeleaf templates are used to
 * build HTML documents which are then translated to PDF by the OpenHTMLtoPDF Library. Importantly, we're using the
 * second-highest possible level of PDF accessibility, which means that when developing new templates, we must listen
 * carefully to the warnings output by the compiler.
 * TODO
 */
@Slf4j
@Service
@SuppressWarnings("PMD.PreserveStackTrace")
public class FileCreationService {

    @Autowired
    private DataManagementService dataManagementService;

    private static final String PATH_TO_LANGUAGES = "templates/languages/";

    /**
     * Wrapper class for the entire json to pdf process.
     *
     * @param inputPayloadUuid UUID representing a particular artefact ID.
     * @return byteArray representing the generated PDF.
     * @throws IOException - uses file streams so needs this.
     */
    public String jsonToHtml(UUID inputPayloadUuid) throws IOException {
        String rawJson = dataManagementService.getArtefactJsonBlob(inputPayloadUuid);
        Artefact artefact = dataManagementService.getArtefact(inputPayloadUuid);
        Location location = dataManagementService.getLocation(artefact.getLocationId());

        Map<String, Object> language = handleLanguages(artefact.getListType(), artefact.getLanguage());

        JsonNode topLevelNode = new ObjectMapper().readTree(rawJson);
        Language languageEntry = artefact.getLanguage();
        String locationName = (languageEntry == Language.ENGLISH) ? location.getName() : location.getWelshName();
        Map<String, String> metadataMap = Map.of(
            "contentDate", DateHelper.formatLocalDateTimeToBst(artefact.getContentDate()),
            "provenance", artefact.getProvenance(),
            "locationName", locationName,
            "language", languageEntry.toString()
        );

        Converter converter = artefact.getListType().getConverter();

        return (converter == null)
            ? parseThymeleafTemplate(rawJson)
            : converter.convert(topLevelNode, metadataMap, language);
    }

    private Map<String, Object> handleLanguages(ListType listType, Language language) throws IOException {
        String path;
        String languageString = GeneralHelper.listTypeToCamelCase(listType);
        if (language.equals(Language.ENGLISH)) {
            path = PATH_TO_LANGUAGES + "en/" + languageString + ".json";
        } else {
            path = PATH_TO_LANGUAGES + "cy/" + languageString + ".json";
        }
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream(path)) {
            return new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
    }

    /**
     * Class which takes in JSON input and uses it to inform a given template. Consider this a placeholder until we
     * have specific style guides created.
     *
     * @param json - json string input representing a publication
     * @return formatted html string representing the input to the pdf reader
     */
    private String parseThymeleafTemplate(String json) {
        SpringTemplateEngine templateEngine = new ThymeleafConfiguration().templateEngine();
        Context context = new Context();
        context.setVariable("jsonBody", json);
        return templateEngine.process("testTemplate.html", context);
    }

    /**
     * Class which takes in an HTML file and generates an accessible PDF file (as a byteArray). This originally used
     * an Opentype font (.otf) but it reduced the file size to switch to a Truetype font (.ttf).
     *
     * @param html - string input representing a well-formed HTML file conforming to WCAG pdf accessibility guidance
     * @return a byte array representing the generated PDF.
     * @throws IOException - if errors appear during the process.
     */
    public byte[] generatePdfFromHtml(String html, boolean accessibility) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode()
                .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_1_A);

            File file = new File("/opt/app/gdsFont.ttf");
            if (file.exists()) {
                builder.useFont(file, "GDS Transport");
            } else {
                builder.useFont(new File(Thread.currentThread().getContextClassLoader()
                                             .getResource("gdsFont.ttf").getFile()), "GDS Transport");
            }
            if (accessibility) {
                builder.usePdfUaAccessbility(true);
            }

            builder.withHtmlContent(html, null)
                .toStream(baos)
                .run();
            return baos.toByteArray();
        }
    }

    /**
     * Takes in a payload id and generates an Excel spreadsheet returned as a byte array.
     *
     * @param payloadId The ID of the artefact to create a spreadsheet from.
     * @return A byte array of the Excel spreadsheet.
     * @throws IOException If an error appears during the process.
     */
    public byte[] generateExcelSpreadsheet(UUID payloadId) throws IOException {
        String rawJson = dataManagementService.getArtefactJsonBlob(payloadId);
        Artefact artefact = dataManagementService.getArtefact(payloadId);

        JsonNode topLevelNode = new ObjectMapper().readTree(rawJson);
        Converter converter = artefact.getListType().getConverter();

        return converter.convertToExcel(topLevelNode);
    }
}
