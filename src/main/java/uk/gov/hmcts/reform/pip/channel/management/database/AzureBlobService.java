package uk.gov.hmcts.reform.pip.channel.management.database;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

/**
 * Class with handles the interaction with the Azure Blob Service.
 */
@Component
public class AzureBlobService {

    private final BlobContainerClient blobContainerClient;

    private static final String DELETE_MESSAGE = "Blob: %s successfully deleted.";

    @Autowired
    public AzureBlobService(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }

    /**
     * Uploads the Excel/PDFs in the Azure blob service.
     *
     * @param payloadId The identifier of the payload
     * @param filePayload The payload of the file to store
     */
    public void uploadFile(String payloadId, byte[] filePayload) {
        BlobClient blobClient = blobContainerClient.getBlobClient(payloadId);

        blobClient.upload(new ByteArrayInputStream(filePayload), filePayload.length, true);
    }

    /**
     * Get the file from the blobstore by the fileId.
     *
     * @param fileId The id of the file to retrieve
     * @return The file from the blob store
     */
    public Resource getBlobFile(String fileId) {
        BlobClient blobClient = blobContainerClient.getBlobClient(fileId);
        byte[] data = blobClient.downloadContent().toBytes();
        return new ByteArrayResource(data);
    }

    /**
     * Delete a blob from the blob store by the fileId.
     *
     * @param fileId The id of the blob to delete
     * @return A confirmation message of the blob deletion
     */
    public String deleteBlob(String fileId) {
        BlobClient blobClient = blobContainerClient.getBlobClient(fileId);
        blobClient.delete();
        return String.format(DELETE_MESSAGE, fileId);
    }
}
