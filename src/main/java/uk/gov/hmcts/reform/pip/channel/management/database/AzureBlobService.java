package uk.gov.hmcts.reform.pip.channel.management.database;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

/**
 * Class with handles the interaction with the Azure Blob Storage.
 */
@Component
public class AzureBlobService {

    private final BlobContainerClient blobContainerClient;

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
    public String uploadFile(String payloadId, byte[] filePayload) {
        BlobClient blobClient = blobContainerClient.getBlobClient(payloadId);

        blobClient.upload(new ByteArrayInputStream(filePayload), filePayload.length, true);

        return payloadId;
    }

    /**
     * Get the file from the blobstore by the fileId.
     *
     * @param fileId The id of the file to retrieve
     * @return The file from the blob store as a byte array
     */
    public byte[] getBlobFile(String fileId) {
        BlobClient blobClient = blobContainerClient.getBlobClient(fileId);
        return blobClient.downloadContent().toBytes();
    }
}
