package uk.gov.hmcts.reform.pip.channel.management.services;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.Application;
import uk.gov.hmcts.reform.pip.channel.management.config.AzureBlobConfigurationTest;
import uk.gov.hmcts.reform.pip.channel.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Artefact;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.ListType;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Location;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {Application.class, AzureBlobConfigurationTest.class})
@ActiveProfiles(profiles = "test")
class PublicationManagementServiceTest {
    @Mock
    private DataManagementService dataManagementService;

    @Mock
    private AzureBlobService azureBlobService;

    @InjectMocks
    private PublicationManagementService publicationManagementService;

    private final Artefact artefact = new Artefact();
    private final Location location = new Location();

    private static final String RESPONSE_MESSAGE = "Response didn't contain expected text";
    private static final String BYTES_NO_MATCH = "Bytes didn't match";
    private static final String TEST = "test";
    private static final byte[] TEST_BYTE = TEST.getBytes();

    private String getInput(String resourcePath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        }
    }

    @BeforeEach
    void setup() {
        artefact.setContentDate(LocalDateTime.now());
        artefact.setLocationId("1");
        artefact.setProvenance("france");
        artefact.setLanguage(Language.ENGLISH);
        artefact.setListType(ListType.SJP_PUBLIC_LIST);

        location.setLocationId(1);
        location.setName("Test");
    }

    @Test
    void testGenerateFilesSjp() throws IOException {
        when(dataManagementService.getArtefactJsonBlob(any()))
            .thenReturn(getInput("/mocks/sjpPublicList.json"));
        when(dataManagementService.getArtefact(any())).thenReturn(artefact);
        when(dataManagementService.getLocation(any())).thenReturn(location);
        when(azureBlobService.uploadFile(any(), any())).thenReturn("Uploaded");

        publicationManagementService.generateFiles(UUID.randomUUID());

        verify(azureBlobService, times(2)).uploadFile(any(), any());
    }

    @Test
    void testGenerateFilesNonSjp() throws IOException {
        artefact.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(dataManagementService.getArtefactJsonBlob(any()))
            .thenReturn(getInput("/mocks/civilDailyCauseList.json"));
        when(dataManagementService.getArtefact(any())).thenReturn(artefact);
        when(dataManagementService.getLocation(any())).thenReturn(location);
        when(azureBlobService.uploadFile(any(), any())).thenReturn("Uploaded");

        publicationManagementService.generateFiles(UUID.randomUUID());

        verify(azureBlobService, times(1)).uploadFile(any(), any());
    }

    @Test
    void testGenerateArtefactSummary() throws IOException {
        when(dataManagementService.getArtefactJsonBlob(any()))
            .thenReturn(getInput("/mocks/sjpPublicList.json"));
        when(dataManagementService.getArtefact(any())).thenReturn(artefact);

        String response = publicationManagementService.generateArtefactSummary(UUID.randomUUID());

        assertTrue(response.contains("AA1 AA1"), RESPONSE_MESSAGE);
        assertTrue(response.contains("Prosecutor:"), RESPONSE_MESSAGE);
        assertTrue(response.contains("Offence: Offence A, Offence B"), RESPONSE_MESSAGE);
        assertTrue(response.contains("This is a forename2 This is a surname2"), RESPONSE_MESSAGE);
        assertTrue(response.contains("•Defendant: This is a forename4 This is a surname4"), RESPONSE_MESSAGE);
    }

    @Test
    void testGetStoredPublicationsSjp() {
        when(dataManagementService.getArtefact(any())).thenReturn(artefact);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        Map<String, byte[]> response = publicationManagementService
            .getStoredPublications(UUID.randomUUID());

        assertEquals(TEST_BYTE, response.get("PDF"), BYTES_NO_MATCH);
        assertEquals(TEST_BYTE, response.get("EXCEL"), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationsNonSjp() {
        artefact.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(dataManagementService.getArtefact(any())).thenReturn(artefact);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        Map<String, byte[]> response = publicationManagementService
            .getStoredPublications(UUID.randomUUID());

        assertEquals(TEST_BYTE, response.get("PDF"), BYTES_NO_MATCH);
        assertTrue(response.get("EXCEL").length == 0, BYTES_NO_MATCH);
    }
}
