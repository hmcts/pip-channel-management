package uk.gov.hmcts.reform.pip.channel.management.services;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.Application;
import uk.gov.hmcts.reform.pip.channel.management.config.AzureBlobTestConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.UnauthorisedException;
import uk.gov.hmcts.reform.pip.channel.management.models.FileType;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Artefact;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.ListType;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Location;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {Application.class, AzureBlobTestConfiguration.class})
@ActiveProfiles(profiles = "test")
class PublicationManagementServiceTest {
    @Mock
    private DataManagementService dataManagementService;

    @Mock
    private AccountManagementService accountManagementService;

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
        location.setRegion(Collections.singletonList("Test"));
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

        Map<FileType, byte[]> response = publicationManagementService
            .getStoredPublications(UUID.randomUUID(), TEST, true);

        assertEquals(TEST_BYTE, response.get(FileType.PDF), BYTES_NO_MATCH);
        assertEquals(TEST_BYTE, response.get(FileType.EXCEL), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationsNonSjp() {
        artefact.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(dataManagementService.getArtefact(any())).thenReturn(artefact);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        Map<FileType, byte[]> response = publicationManagementService
            .getStoredPublications(UUID.randomUUID(), TEST, true);

        assertEquals(TEST_BYTE, response.get(FileType.PDF), BYTES_NO_MATCH);
        assertTrue(response.get(FileType.EXCEL).length == 0, BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationsAuthorisedPublic() {
        artefact.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        artefact.setSensitivity(Sensitivity.PUBLIC);
        when(dataManagementService.getArtefact(any())).thenReturn(artefact);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        Map<FileType, byte[]> response = publicationManagementService
            .getStoredPublications(UUID.randomUUID(), TEST, false);

        assertEquals(TEST_BYTE, response.get(FileType.PDF), BYTES_NO_MATCH);
        assertTrue(response.get(FileType.EXCEL).length == 0, BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationsAuthorisedUserIdNull() {
        artefact.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        artefact.setSensitivity(Sensitivity.CLASSIFIED);
        when(dataManagementService.getArtefact(any())).thenReturn(artefact);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        UnauthorisedException ex = assertThrows(UnauthorisedException.class, () ->
            publicationManagementService
                .getStoredPublications(UUID.randomUUID(), null, false),
                                              "Expected exception to be thrown");

        assertTrue(ex.getMessage().contains("User with id null is not authorised to access artefact with id"),
                   "Message should contain expected");
    }

    @Test
    void testGetStoredPublicationsAuthorisedFalse() {
        artefact.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        artefact.setSensitivity(Sensitivity.CLASSIFIED);
        when(dataManagementService.getArtefact(any())).thenReturn(artefact);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);
        when(accountManagementService.getIsAuthorised(any(), any(), any())).thenReturn(false);

        UnauthorisedException ex = assertThrows(UnauthorisedException.class, () ->
                                                  publicationManagementService
                                                      .getStoredPublications(UUID.randomUUID(),
                                                                             UUID.randomUUID().toString(),
                                                                             false),
                                              "Expected exception to be thrown");

        assertTrue(ex.getMessage().contains("is not authorised to access artefact with id"),
                   "Message should contain expected");
    }
}
