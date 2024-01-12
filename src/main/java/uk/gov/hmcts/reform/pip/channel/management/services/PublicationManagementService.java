package uk.gov.hmcts.reform.pip.channel.management.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.FileSizeLimitException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.ProcessingException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.UnauthorisedException;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.ArtefactSummaryConverter;
import uk.gov.hmcts.reform.pip.model.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.publication.FileType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

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
    private static final String ADDITIONAL_PDF_SUFFIX = "_cy";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AzureBlobService azureBlobService;
    private final DataManagementService dataManagementService;
    private final AccountManagementService accountManagementService;
    private final ListConversionFactory listConversionFactory;
    private final PublicationFileGenerationService publicationFileGenerationService;

    @Autowired
    public PublicationManagementService(AzureBlobService azureBlobService,
                                        DataManagementService dataManagementService,
                                        AccountManagementService accountManagementService,
                                        ListConversionFactory listConversionFactory,
                                        PublicationFileGenerationService publicationFileGenerationService) {
        this.azureBlobService = azureBlobService;
        this.dataManagementService = dataManagementService;
        this.accountManagementService = accountManagementService;
        this.listConversionFactory = listConversionFactory;
        this.publicationFileGenerationService = publicationFileGenerationService;
    }

    /**
     * Generate and store the PDF/Excel files for a given artefact.
     *
     * @param artefactId The artefact Id to generate the files for.
     */
    public void generateFiles(UUID artefactId) {
        publicationFileGenerationService.generate(artefactId).ifPresent(files -> {
            if (files.getPrimaryPdf().length > 0) {
                azureBlobService.uploadFile(artefactId + PDF.getExtension(), files.getPrimaryPdf());
            }

            if (files.getAdditionalPdf().length > 0) {
                azureBlobService.uploadFile(artefactId + ADDITIONAL_PDF_SUFFIX + PDF.getExtension(),
                                            files.getAdditionalPdf());
            }

            if (files.getExcel().length > 0) {
                azureBlobService.uploadFile(artefactId + EXCEL.getExtension(), files.getExcel());
            }
        });
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
     * Delete all publication files for a given artefact.
     *
     * @param artefactId The artefact ID to delete the files for.
     */
    public void deleteFiles(UUID artefactId) {
        Artefact artefact = dataManagementService.getArtefact(artefactId);
        azureBlobService.deleteBlobFile(artefact.getArtefactId() + PDF.getExtension());

        if (artefact.getListType().hasAdditionalPdf() && artefact.getLanguage() != Language.ENGLISH) {
            azureBlobService.deleteBlobFile(artefact.getArtefactId() + ADDITIONAL_PDF_SUFFIX
                                                + PDF.getExtension());
        }

        if (artefact.getListType().hasExcel()) {
            azureBlobService.deleteBlobFile(artefact.getArtefactId() + EXCEL.getExtension());
        }
    }

    public void deleteFiles(UUID artefactId, ListType listType, Language language) {
        azureBlobService.deleteBlobFile(artefactId + PDF.getExtension());

        if (listType.hasAdditionalPdf() && language != Language.ENGLISH) {
            azureBlobService.deleteBlobFile(artefactId + ADDITIONAL_PDF_SUFFIX
                                                + PDF.getExtension());
        }

        if (listType.hasExcel()) {
            azureBlobService.deleteBlobFile(artefactId + EXCEL.getExtension());
        }
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
}
