package uk.gov.hmcts.reform.pip.channel.management.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.FileSizeLimitException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ProcessingException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.UnauthorisedException;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.ArtefactSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.FileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.model.location.Location;
import uk.gov.hmcts.reform.pip.model.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.publication.FileType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.pip.model.publication.FileType.EXCEL;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.PDF;

@Slf4j
@Service
@SuppressWarnings({"PMD.PreserveStackTrace"})
public class PublicationManagementService {
    private static final int MAX_FILE_SIZE = 2_000_000;
    private static final String ADDITIONAL_PDF_SUFFIX = "_cy";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AzureBlobService azureBlobService;
    private final DataManagementService dataManagementService;
    private final AccountManagementService accountManagementService;
    private final ListConversionFactory listConversionFactory;

    @Value("${pdf.font}")
    private String pdfFont;

    @Autowired
    public PublicationManagementService(AzureBlobService azureBlobService,
                                        DataManagementService dataManagementService,
                                        AccountManagementService accountManagementService,
                                        ListConversionFactory listConversionFactory) {
        this.azureBlobService = azureBlobService;
        this.dataManagementService = dataManagementService;
        this.accountManagementService = accountManagementService;
        this.listConversionFactory = listConversionFactory;
    }

    /**
     * Generate and store the PDF/Excel files for a given artefact.
     *
     * @param artefactId The artefact Id to generate the files for.
     */
    public void generateFiles(UUID artefactId) {
        String rawJson = dataManagementService.getArtefactJsonBlob(artefactId);
        Artefact artefact = dataManagementService.getArtefact(artefactId);
        Location location = dataManagementService.getLocation(artefact.getLocationId());
        JsonNode topLevelNode;

        try {
            topLevelNode = MAPPER.readTree(rawJson);
            FileConverter fileConverter = listConversionFactory.getFileConverter(artefact.getListType());

            if (fileConverter == null) {
                log.error("Failed to find converter for list type");
                return;
            }

            // Generate the Excel and store it
            byte[] outputExcel = fileConverter.convertToExcel(topLevelNode, artefact.getListType());
            if (outputExcel.length > 0) {
                azureBlobService.uploadFile(artefactId + EXCEL.getExtension(), outputExcel);
            }

            // Generate the English and/or Welsh PDFs and store in Azure blob storage
            Pair<byte[], byte[]> pdfs = generatePdfs(topLevelNode, artefact, location);

            if (pdfs.getLeft().length > 0) {
                azureBlobService.uploadFile(artefactId + PDF.getExtension(), pdfs.getLeft());
            }

            if (pdfs.getRight().length > 0) {
                azureBlobService.uploadFile(artefactId + ADDITIONAL_PDF_SUFFIX + PDF.getExtension(), pdfs.getRight());
            }
        } catch (IOException ex) {
            throw new ProcessingException(String.format("Failed to generate files for artefact id %s", artefactId));
        }
    }

    /**
     * Generate the artefact summary by provided artefact id.
     *
     * @param artefactId The artefact Id to generate the summary for.
     * @return A string of the generated summary
     */
    public String generateArtefactSummary(UUID artefactId) {
        Artefact artefact = dataManagementService.getArtefact(artefactId);
        String summary = "";
        ArtefactSummaryConverter artefactSummaryConverter = listConversionFactory.getArtefactSummaryConverter(
            artefact.getListType()
        );

        if (artefactSummaryConverter == null) {
            log.error("Failed to find converter for list type");
            return summary;
        }

        try {
            String rawJson = dataManagementService.getArtefactJsonBlob(artefactId);
            summary = artefactSummaryConverter.convert(MAPPER.readTree(rawJson));
        } catch (JsonProcessingException ex) {
            throw new ProcessingException(String.format("Failed to generate summary for artefact id %s", artefactId));
        }
        return summary;
    }

    /**
     * Get the stored file (PDF or Excel) for an artefact.
     *
     * @param artefactId The artefact ID to get the file for.
     * @param fileType The type of File. Can be either PDF or Excel.
     * @param maxFileSize The file size limit to return the file.
     * @param userId The ID of user performing the operation.
     * @param system Is system user?
     * @param additionalPdf Is getting the additional Welsh PDF?
     * @return A Base64 encoded string of the file.
     */
    public String getStoredPublication(UUID artefactId, FileType fileType, Integer maxFileSize, String userId,
                                       boolean system, boolean additionalPdf) {
        Artefact artefact = dataManagementService.getArtefact(artefactId);
        if (!isAuthorised(artefact, userId, system)) {
            throw new UnauthorisedException(
                String.format("User with id %s is not authorised to access artefact with id %s", userId, artefactId)
            );
        }

        String filename = fileType == PDF && additionalPdf
            ? artefactId + ADDITIONAL_PDF_SUFFIX : artefactId.toString();
        byte[] file = azureBlobService.getBlobFile(filename + fileType.getExtension());
        if (maxFileSize != null && file.length > maxFileSize) {
            throw new FileSizeLimitException(
                String.format("File with type %s for artefact with id %s has size over the limit of %s bytes",
                              fileType, artefactId, maxFileSize)
            );
        }
        return Base64.getEncoder().encodeToString(file);
    }

    /**
     * Get the sorted files for an artefact.
     *
     * @param artefactId The artefact Id to get the files for.
     * @return A map of the filetype to file byte array
     */
    public Map<FileType, byte[]> getStoredPublications(UUID artefactId, String userId, boolean system) {
        Artefact artefact = dataManagementService.getArtefact(artefactId);
        if (isAuthorised(artefact, userId, system)) {
            Map<FileType, byte[]> publicationFilesMap = new ConcurrentHashMap<>();
            publicationFilesMap.put(PDF, azureBlobService.getBlobFile(artefactId + ".pdf"));
            publicationFilesMap.put(EXCEL, artefact.getListType().hasExcel()
                ? azureBlobService.getBlobFile(artefactId + ".xlsx") : new byte[0]);
            return publicationFilesMap;
        } else {
            throw new UnauthorisedException(String.format("User with id %s is not authorised to access artefact with id"
                                                              + " %s", userId, artefactId));
        }
    }

    /**
     * Generate the English and/or Welsh PDF for a given artefact.
     *
     * @param topLevelNode The data node.
     * @param artefact The artefact.
     * @param location The location.
     * @return a byte array of the generated pdf.
     * @throws IOException error.
     */
    private Pair<byte[], byte[]> generatePdfs(JsonNode topLevelNode, Artefact artefact, Location location)
        throws IOException {
        Language language = artefact.getLanguage();

        if (artefact.getListType().hasAdditionalPdf() && language != Language.ENGLISH) {
            byte[] englishPdf = generatePdf(topLevelNode, artefact, location, Language.ENGLISH, true);
            if (englishPdf.length > MAX_FILE_SIZE) {
                englishPdf = generatePdf(topLevelNode, artefact, location, Language.ENGLISH, false);
            }

            byte[] welshPdf = generatePdf(topLevelNode, artefact, location, Language.WELSH, true);
            if (welshPdf.length > MAX_FILE_SIZE) {
                welshPdf = generatePdf(topLevelNode, artefact, location, Language.WELSH, false);
            }

            return Pair.of(englishPdf, welshPdf);
        }

        byte[] pdf = generatePdf(topLevelNode, artefact, location, language, true);
        if (pdf.length > MAX_FILE_SIZE) {
            pdf = generatePdf(topLevelNode, artefact, location, language, false);
        }

        return Pair.of(pdf, new byte[0]);
    }

    /**
     * Generate the PDF for a given artefact.
     *
     * @param topLevelNode The data node.
     * @param artefact The artefact.
     * @param location The location where the artefact is uploaded to.
     * @param language The language of the artefact.
     * @param accessibility If the pdf should be accessibility generated.
     * @return a byte array of the generated pdf.
     * @throws IOException Throw if error generating.
     */
    private byte[] generatePdf(JsonNode topLevelNode, Artefact artefact, Location location, Language language,
                               boolean accessibility) throws IOException {
        Map<String, Object> languageResource = LanguageResourceHelper.getLanguageResources(
            artefact.getListType(), language);
        String html = listConversionFactory.getFileConverter(artefact.getListType())
            .convert(topLevelNode, buildArtefactMetadata(artefact, location, language), languageResource);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode()
                .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_1_A);

            File file = new File(pdfFont);
            if (file.exists()) {
                builder.useFont(file, "openSans");
            } else {
                builder.useFont(new File(Thread.currentThread().getContextClassLoader()
                                             .getResource("openSans.ttf").getFile()), "openSans");
            }
            builder.usePdfUaAccessbility(accessibility);
            builder.withHtmlContent(html, null)
                .toStream(baos)
                .run();
            return baos.toByteArray();
        }
    }

    private Map<String, String> buildArtefactMetadata(Artefact artefact, Location location, Language language) {
        String locationName = (language == Language.ENGLISH) ? location.getName() : location.getWelshName();
        String provenance = maskDataSourceName(artefact.getProvenance());
        return Map.of(
            "contentDate", DateHelper.formatLocalDateTimeToBst(artefact.getContentDate()),
            "provenance", provenance,
            "locationName", locationName,
            "region", String.join(", ", location.getRegion()),
            "regionName", String.join(", ", location.getRegion()),
            "language", language.toString(),
            "listType", artefact.getListType().name()
        );
    }

    private boolean isAuthorised(Artefact artefact, String userId, boolean system) {
        if (system || artefact.getSensitivity().equals(Sensitivity.PUBLIC)) {
            return true;
        } else if (userId == null) {
            return false;
        }
        return accountManagementService.getIsAuthorised(UUID.fromString(userId), artefact.getListType(),
                                                        artefact.getSensitivity());
    }

    public static String maskDataSourceName(String provenance) {
        return "SNL".equals(provenance) ? "ListAssist" : provenance;
    }
}
