package uk.gov.hmcts.reform.pip.channel.management.database;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.NotFoundException;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AzureBlobServiceTest {

    private static final String BLOB_NAME = UUID.randomUUID().toString();

    private static final String MESSAGES_MATCH = "Messages should match";

    private static final byte[] TEST_BYTE = MESSAGES_MATCH.getBytes();

    @Mock
    BlobContainerClient blobContainerClient;

    @Mock
    BlobClient blobClient;

    @InjectMocks
    AzureBlobService azureBlobService;

    @BeforeEach
    void setup() {
        when(blobContainerClient.getBlobClient(BLOB_NAME)).thenReturn(blobClient);
    }

    @Test
    void testCreationOfNewBlobFile() {
        String response = azureBlobService.uploadFile(BLOB_NAME, TEST_BYTE);

        assertEquals(BLOB_NAME, response, MESSAGES_MATCH);
    }

    @Test
    void testGetBlobFile() {
        BinaryData binaryData = BinaryData.fromString(MESSAGES_MATCH);
        when(blobClient.downloadContent()).thenReturn(binaryData);
        byte[] blobFile = azureBlobService.getBlobFile(BLOB_NAME);

        assertNotNull(blobFile, "Return was null");
    }

    @Test
    void testGetBlobFileNotFound() {
        doThrow(BlobStorageException.class).when(blobClient).downloadContent();

        assertThatThrownBy(() -> azureBlobService.getBlobFile(BLOB_NAME))
            .isInstanceOf(NotFoundException.class)
            .hasMessage(String.format("Blob file with id %s not found", BLOB_NAME));
    }
}
