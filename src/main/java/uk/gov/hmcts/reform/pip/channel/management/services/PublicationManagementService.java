package uk.gov.hmcts.reform.pip.channel.management.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ProcessingException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.UnauthorisedException;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.ArtefactSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.FileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.model.location.Location;
import uk.gov.hmcts.reform.pip.model.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.publication.FileType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.SJP_PRESS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SJP_PUBLIC_LIST;

@Slf4j
@Service
@SuppressWarnings({"PMD.PreserveStackTrace"})
public class PublicationManagementService {
    private static final int MAX_FILE_SIZE = 2_000_000;

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
            topLevelNode = new ObjectMapper().readTree(rawJson);
            FileConverter fileConverter = listConversionFactory.getFileConverter(artefact.getListType());

            if (fileConverter == null) {
                log.error("Failed to find converter for list type");
                return;
            }

            // Generate the Excel and store it
            byte[] outputExcel = fileConverter.convertToExcel(topLevelNode);
            if (outputExcel.length > 0) {
                azureBlobService.uploadFile(artefactId + ".xlsx", outputExcel);
            }

            // Generate the PDF and store it
            byte[] outputPdf = generatePdf(topLevelNode, artefact, location, true);
            if (outputPdf.length > MAX_FILE_SIZE) {
                outputPdf = generatePdf(topLevelNode, artefact, location, false);
            }

            if (outputPdf.length > 0) {
                azureBlobService.uploadFile(artefactId + ".pdf", outputPdf);
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
            summary = artefactSummaryConverter.convert(rawJson);
        } catch (JsonProcessingException ex) {
            throw new ProcessingException(String.format("Failed to generate summary for artefact id %s", artefactId));
        }
        return summary;
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
            publicationFilesMap.put(FileType.PDF, azureBlobService.getBlobFile(artefactId + ".pdf"));

            if (SJP_PUBLIC_LIST.equals(artefact.getListType())
                || SJP_PRESS_LIST.equals(artefact.getListType())) {
                publicationFilesMap.put(FileType.EXCEL, azureBlobService.getBlobFile(artefactId + ".xlsx"));
            } else {
                publicationFilesMap.put(FileType.EXCEL, new byte[0]);
            }
            return publicationFilesMap;
        } else {
            throw new UnauthorisedException(String.format("User with id %s is not authorised to access artefact with id"
                                                            + " %s", userId, artefactId));
        }
    }

    /**
     * Generate the pdf for a given artefact.
     *
     * @param topLevelNode The data node.
     * @param artefact The artefact.
     * @param location The location.
     * @param accessibility If the pdf should be accessibility generated.
     * @return a byte array of the generated pdf.
     * @throws IOException Throw if error generating.
     */
    private byte[] generatePdf(JsonNode topLevelNode, Artefact artefact,
                               Location location, boolean accessibility) throws IOException {
        Map<String, Object> language = LanguageResourceHelper.getLanguageResources(
            artefact.getListType(), artefact.getLanguage());
        Language languageEntry = artefact.getLanguage();
        String locationName = (languageEntry == Language.ENGLISH) ? location.getName() : location.getWelshName();
        Map<String, String> metadataMap = Map.of(
            "contentDate", DateHelper.formatLocalDateTimeToBst(artefact.getContentDate()),
            "provenance", artefact.getProvenance(),
            "locationName", locationName,
            "region", String.join(", ", location.getRegion()),
            "regionName", String.join(", ", location.getRegion()),
            "language", languageEntry.toString(),
            "listType", artefact.getListType().name()
        );

        String html = listConversionFactory.getFileConverter(artefact.getListType())
            .convert(topLevelNode, metadataMap, language);
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

            if (accessibility) {
                builder.usePdfUaAccessbility(true);
            }

            builder.withHtmlContent(html, null)
                .toStream(baos)
                .run();
            return baos.toByteArray();
        }
    }

    private boolean isAuthorised(Artefact artefact, String userId, boolean system) {
        if (system || artefact.getSensitivity().equals(Sensitivity.PUBLIC)) {
            return true;
        } else if (userId == null) {
            return false;
        } else {
            return accountManagementService.getIsAuthorised(UUID.fromString(userId), artefact.getListType(),
                                                            artefact.getSensitivity());
        }
    }
}
