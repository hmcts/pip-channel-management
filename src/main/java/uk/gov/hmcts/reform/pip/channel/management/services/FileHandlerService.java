package uk.gov.hmcts.reform.pip.channel.management.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.database.AzureBlobService;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class FileHandlerService {

    @Autowired
    FileCreationService fileCreationService;

    @Autowired
    AzureBlobService azureBlobService;

    /**
     * Method to handle the creation and storing of Excel/PDFs for an artefact.
     *
     * @param artefactId The ID of the artefact to generate files for.
     */
    public void generateFiles(UUID artefactId) throws IOException {
        byte[] outputExcel = fileCreationService.generateExcelSpreadsheet(artefactId);
        azureBlobService.uploadFile(artefactId.toString() + ".xlsx", outputExcel);

        // Upload a file which has artefactId+PDF/EXCEL extension. Can do same way as JSON
        // Retrieve a file based off artefactId and return list of x


    }
}
