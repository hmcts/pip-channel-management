package uk.gov.hmcts.reform.pip.channel.management.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {Application.class, AzureBlobTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
class PublicationManagementServiceTest {
    @MockBean
    private DataManagementService dataManagementService;

    @MockBean
    private AccountManagementService accountManagementService;

    @MockBean
    private AzureBlobService azureBlobService;

    @Autowired
    private PublicationManagementService publicationManagementService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Artefact ARTEFACT = new Artefact();
    private static final Location LOCATION = new Location();

    private static final String RESPONSE_MESSAGE = "Response didn't contain expected text";
    private static final String BYTES_NO_MATCH = "Bytes didn't match";
    private static final String TEST = "test";
    private static final byte[] TEST_BYTE = TEST.getBytes();

    private static final UUID TEST_ARTEFACT_ID = UUID.randomUUID();
    private static final String TEST_USER_ID = UUID.randomUUID().toString();

    private String getInput(String resourcePath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        }
    }

    @BeforeAll
    static void startup() {
        OBJECT_MAPPER.findAndRegisterModules();
    }

    @BeforeEach
    void setup() {
        ARTEFACT.setContentDate(LocalDateTime.now());
        ARTEFACT.setLocationId("1");
        ARTEFACT.setProvenance("france");
        ARTEFACT.setLanguage(Language.ENGLISH);
        ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);

        LOCATION.setLocationId(1);
        LOCATION.setName("Test");
        LOCATION.setRegion(Collections.singletonList("Test"));
    }

    @Test
    void testGenerateFilesSjp() throws IOException {
        when(dataManagementService.getArtefactJsonBlob(any()))
            .thenReturn(getInput("/mocks/sjpPublicList.json"));
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(dataManagementService.getLocation(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn("Uploaded");

        publicationManagementService.generateFiles(TEST_ARTEFACT_ID);

        verify(azureBlobService, times(2)).uploadFile(any(), any());
    }

    @Test
    void testGenerateFilesNonSjp() throws IOException {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(dataManagementService.getArtefactJsonBlob(any()))
            .thenReturn(getInput("/mocks/civilDailyCauseList.json"));
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(dataManagementService.getLocation(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn("Uploaded");

        publicationManagementService.generateFiles(TEST_ARTEFACT_ID);

        verify(azureBlobService, times(1)).uploadFile(any(), any());
    }

    @Test
    void testGenerateFilesWithoutConverter() throws IOException {
        ARTEFACT.setListType(ListType.SJP_PRESS_REGISTER);
        when(dataManagementService.getArtefactJsonBlob(any()))
            .thenReturn(getInput("/mocks/sjpPublicList.json"));
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(dataManagementService.getLocation(any())).thenReturn(LOCATION);

        publicationManagementService.generateFiles(TEST_ARTEFACT_ID);
        verifyNoInteractions(azureBlobService);
    }

    @Test
    void testGenerateArtefactSummary() throws IOException {
        when(dataManagementService.getArtefactJsonBlob(any()))
            .thenReturn(getInput("/mocks/sjpPublicList.json"));
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);

        String response = publicationManagementService.generateArtefactSummary(TEST_ARTEFACT_ID);

        assertTrue(response.contains("AA1 AA1"), RESPONSE_MESSAGE);
        assertTrue(response.contains("Prosecutor:"), RESPONSE_MESSAGE);
        assertTrue(response.contains("Offence: Offence A, Offence B"), RESPONSE_MESSAGE);
        assertTrue(response.contains("This is a forename2 This is a surname2"), RESPONSE_MESSAGE);
        assertTrue(response.contains("•Defendant: This is a forename4 This is a surname4"), RESPONSE_MESSAGE);
    }

    @Test
    void testGenerateArtefactSummaryWithoutConverter() {
        ARTEFACT.setListType(ListType.SJP_PRESS_REGISTER);
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);

        assertEquals("", publicationManagementService.generateArtefactSummary(TEST_ARTEFACT_ID),
                     RESPONSE_MESSAGE);
        verify(dataManagementService, never()).getArtefactJsonBlob(TEST_ARTEFACT_ID);
    }

    @ParameterizedTest
    @MethodSource("sjpParameters")
    void testGetStoredPublicationsSjp(Artefact artefact) {
        when(dataManagementService.getArtefact(any())).thenReturn(artefact);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        Map<FileType, byte[]> response = publicationManagementService
            .getStoredPublications(TEST_ARTEFACT_ID, TEST, true);

        assertEquals(TEST_BYTE, response.get(FileType.PDF), BYTES_NO_MATCH);
        assertEquals(TEST_BYTE, response.get(FileType.EXCEL), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationsNonSjp() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        Map<FileType, byte[]> response = publicationManagementService
            .getStoredPublications(TEST_ARTEFACT_ID, TEST, true);

        assertEquals(TEST_BYTE, response.get(FileType.PDF), BYTES_NO_MATCH);
        assertEquals(0, response.get(FileType.EXCEL).length, BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationsAuthorisedPublic() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        ARTEFACT.setSensitivity(Sensitivity.PUBLIC);
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        Map<FileType, byte[]> response = publicationManagementService
            .getStoredPublications(TEST_ARTEFACT_ID, TEST, false);

        assertEquals(TEST_BYTE, response.get(FileType.PDF), BYTES_NO_MATCH);
        assertEquals(0, response.get(FileType.EXCEL).length, BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationsAuthorisedUserIdNull() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        ARTEFACT.setSensitivity(Sensitivity.CLASSIFIED);
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        UnauthorisedException ex = assertThrows(UnauthorisedException.class, () ->
            publicationManagementService
                .getStoredPublications(TEST_ARTEFACT_ID, null, false),
                                              "Expected exception to be thrown");

        assertTrue(ex.getMessage().contains("User with id null is not authorised to access artefact with id"),
                   "Message should contain expected");
    }

    @Test
    void testGetStoredPublicationsAuthorisedFalse() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        ARTEFACT.setSensitivity(Sensitivity.CLASSIFIED);
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);
        when(accountManagementService.getIsAuthorised(any(), any(), any())).thenReturn(false);

        UnauthorisedException ex = assertThrows(UnauthorisedException.class, () ->
                                                  publicationManagementService
                                                      .getStoredPublications(TEST_ARTEFACT_ID, TEST_USER_ID, false),
                                              "Expected exception to be thrown");

        assertTrue(ex.getMessage().contains("is not authorised to access artefact with id"),
                   "Message should contain expected");
    }

    private static Stream<Arguments> sjpParameters() throws JsonProcessingException {
        Artefact sjpPublicArtefact = ARTEFACT;
        sjpPublicArtefact.setListType(ListType.SJP_PUBLIC_LIST);

        Artefact sjpPressArtefact = OBJECT_MAPPER.readValue(
            OBJECT_MAPPER.writeValueAsString(ARTEFACT), Artefact.class
        );
        sjpPressArtefact.setListType(ListType.SJP_PRESS_LIST);

        return Stream.of(
            Arguments.of(sjpPublicArtefact),
            Arguments.of(sjpPressArtefact)
        );
    }
}
