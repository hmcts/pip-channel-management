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
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.FileSizeLimitException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.exceptions.UnauthorisedException;
import uk.gov.hmcts.reform.pip.model.location.Location;
import uk.gov.hmcts.reform.pip.model.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.publication.FileType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.EXCEL;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.PDF;

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
    private static final Artefact WELSH_ARTEFACT = new Artefact();
    private static final Location LOCATION = new Location();

    private static final String RESPONSE_MESSAGE = "Response didn't contain expected text";
    private static final String NOT_FOUND_MESSAGE = "File not found";
    private static final String BYTES_NO_MATCH = "Bytes didn't match";
    private static final String EXCEPTION_NOT_MATCH = "Exception message should contain expected";
    private static final String TEST = "test";
    private static final byte[] TEST_BYTE = TEST.getBytes();

    private static final UUID TEST_ARTEFACT_ID = UUID.randomUUID();
    private static final String TEST_USER_ID = UUID.randomUUID().toString();

    private static final String UPLOADED = "uploaded";
    private static final String WELSH_PDF_SUFFIX = "_cy";

    private static String sjpPublicListInput;
    private static String civilDailyListInput;

    private static String getInput(String resourcePath) throws IOException {
        try (InputStream inputStream = PublicationManagementServiceTest.class.getResourceAsStream(resourcePath)) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        }
    }

    @BeforeAll
    static void startup() throws IOException {
        OBJECT_MAPPER.findAndRegisterModules();
        sjpPublicListInput = getInput("/mocks/sjpPublicList.json");
        civilDailyListInput = getInput("/mocks/civilDailyCauseList.json");
    }

    @BeforeEach
    void setup() {
        ARTEFACT.setContentDate(LocalDateTime.now());
        ARTEFACT.setLocationId("1");
        ARTEFACT.setProvenance("france");
        ARTEFACT.setLanguage(Language.ENGLISH);
        ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);

        WELSH_ARTEFACT.setContentDate(LocalDateTime.now());
        WELSH_ARTEFACT.setLocationId("1");
        WELSH_ARTEFACT.setProvenance("france");
        WELSH_ARTEFACT.setLanguage(Language.WELSH);
        WELSH_ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);

        LOCATION.setLocationId(1);
        LOCATION.setName("Test");
        LOCATION.setWelshName("Test");
        LOCATION.setRegion(Collections.singletonList("Test"));
    }

    @Test
    void testGenerateFilesSjpEnglish() {
        when(dataManagementService.getArtefactJsonBlob(any())).thenReturn(sjpPublicListInput);
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(dataManagementService.getLocation(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn(UPLOADED);

        publicationManagementService.generateFiles(TEST_ARTEFACT_ID);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
        verify(azureBlobService, never())
            .uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
    }

    @Test
    void testGenerateFilesSjpWelsh() {
        when(dataManagementService.getArtefactJsonBlob(any())).thenReturn(sjpPublicListInput);
        when(dataManagementService.getArtefact(any())).thenReturn(WELSH_ARTEFACT);
        when(dataManagementService.getLocation(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn(UPLOADED);

        publicationManagementService.generateFiles(TEST_ARTEFACT_ID);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
        verify(azureBlobService, never())
            .uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
    }

    @Test
    void testGenerateFilesNonSjpEnglish() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(dataManagementService.getArtefactJsonBlob(any())).thenReturn(civilDailyListInput);
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(dataManagementService.getLocation(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn(UPLOADED);

        publicationManagementService.generateFiles(TEST_ARTEFACT_ID);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService, never())
            .uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
        verify(azureBlobService, never()).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
    }

    @Test
    void testGenerateFilesNonSjpWelsh() {
        WELSH_ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(dataManagementService.getArtefactJsonBlob(any())).thenReturn(civilDailyListInput);
        when(dataManagementService.getArtefact(any())).thenReturn(WELSH_ARTEFACT);
        when(dataManagementService.getLocation(any())).thenReturn(LOCATION);
        when(azureBlobService.uploadFile(any(), any())).thenReturn(UPLOADED);

        publicationManagementService.generateFiles(TEST_ARTEFACT_ID);

        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + PDF.getExtension()), any());
        verify(azureBlobService).uploadFile(eq(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()), any());
        verify(azureBlobService, never()).uploadFile(eq(TEST_ARTEFACT_ID + EXCEL.getExtension()), any());
    }

    @Test
    void testGenerateFilesWithoutConverter() {
        ARTEFACT.setListType(ListType.SJP_PRESS_REGISTER);
        when(dataManagementService.getArtefactJsonBlob(any())).thenReturn(sjpPublicListInput);
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(dataManagementService.getLocation(any())).thenReturn(LOCATION);

        publicationManagementService.generateFiles(TEST_ARTEFACT_ID);
        verifyNoInteractions(azureBlobService);
    }

    @Test
    void testGenerateArtefactSummary() {
        when(dataManagementService.getArtefactJsonBlob(any())).thenReturn(sjpPublicListInput);
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

    @Test
    void testGetStoredPdfPublicationSjp() {
        when(dataManagementService.getArtefact(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(TEST_ARTEFACT_ID + PDF.getExtension())).thenReturn(TEST_BYTE);

        String response = publicationManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, null, TEST, true, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredAdditionalPdfPublicationSjp() {
        when(dataManagementService.getArtefact(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        doThrow(new NotFoundException(NOT_FOUND_MESSAGE)).when(azureBlobService)
            .getBlobFile(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
            publicationManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, PDF, null, TEST, true, true
            ));

        assertTrue(ex.getMessage().contains(NOT_FOUND_MESSAGE), EXCEPTION_NOT_MATCH);
    }

    @ParameterizedTest
    @MethodSource("sjpParameters")
    void testGetStoredExcelPublicationSjp(Artefact artefact) {
        when(dataManagementService.getArtefact(TEST_ARTEFACT_ID)).thenReturn(artefact);
        when(azureBlobService.getBlobFile(TEST_ARTEFACT_ID + EXCEL.getExtension())).thenReturn(TEST_BYTE);

        String response = publicationManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, EXCEL, null, TEST, true, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPdfPublicationNonSjp() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(dataManagementService.getArtefact(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(TEST_ARTEFACT_ID + PDF.getExtension())).thenReturn(TEST_BYTE);

        String response = publicationManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, null, TEST, true, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredAdditionalPdfPublicationNonSjp() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(dataManagementService.getArtefact(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(TEST_ARTEFACT_ID + WELSH_PDF_SUFFIX + PDF.getExtension()))
            .thenReturn(TEST_BYTE);

        String response = publicationManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, null, TEST, true, true
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredExcelPublicationNonSjp() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(dataManagementService.getArtefact(TEST_ARTEFACT_ID)).thenReturn(ARTEFACT);
        doThrow(new NotFoundException(NOT_FOUND_MESSAGE)).when(azureBlobService)
            .getBlobFile(TEST_ARTEFACT_ID + EXCEL.getExtension());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
            publicationManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, EXCEL, null, TEST, true, false
            ));

        assertTrue(ex.getMessage().contains(NOT_FOUND_MESSAGE), EXCEPTION_NOT_MATCH);
    }

    @Test
    void testGetStoredPublicationWithinFileSizeLimit() {
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        String response = publicationManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, 20, TEST, true, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationOverFileSizeLimit() {
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        FileSizeLimitException ex = assertThrows(FileSizeLimitException.class, () ->
            publicationManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, PDF, 2, TEST, true, false
            ));

        assertTrue(ex.getMessage().contains("File with type PDF for artefact with id " + TEST_ARTEFACT_ID
                                                + " has size over the limit of 2 bytes"), EXCEPTION_NOT_MATCH);
    }

    @Test
    void testGetStoredPublicationAuthorisedPublic() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        ARTEFACT.setSensitivity(Sensitivity.PUBLIC);
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        String response = publicationManagementService.getStoredPublication(
            TEST_ARTEFACT_ID, PDF, null, TEST, false, false
        );

        byte[] decodedBytes = Base64.getDecoder().decode(response);
        assertEquals(Arrays.toString(TEST_BYTE), Arrays.toString(decodedBytes), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationAuthorisedUserIdNull() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        ARTEFACT.setSensitivity(Sensitivity.CLASSIFIED);
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        UnauthorisedException ex = assertThrows(UnauthorisedException.class, () ->
            publicationManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, PDF, null, null, false, false
            ));

        assertTrue(ex.getMessage().contains("User with id null is not authorised to access artefact with id"),
                   EXCEPTION_NOT_MATCH);
    }

    @Test
    void testGetStoredPublicationAuthorisedFalse() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        ARTEFACT.setSensitivity(Sensitivity.CLASSIFIED);
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);
        when(accountManagementService.getIsAuthorised(any(), any(), any())).thenReturn(false);

        UnauthorisedException ex = assertThrows(UnauthorisedException.class, () ->
            publicationManagementService.getStoredPublication(
                TEST_ARTEFACT_ID, PDF, null, TEST_USER_ID, false, false
            ), "Expected exception to be thrown");

        assertTrue(ex.getMessage().contains("is not authorised to access artefact with id"),
                   EXCEPTION_NOT_MATCH);
    }

    @ParameterizedTest
    @MethodSource("sjpParameters")
    void testGetStoredPublicationsSjp(Artefact artefact) {
        when(dataManagementService.getArtefact(any())).thenReturn(artefact);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        Map<FileType, byte[]> response = publicationManagementService
            .getStoredPublications(TEST_ARTEFACT_ID, TEST, true);

        assertEquals(TEST_BYTE, response.get(PDF), BYTES_NO_MATCH);
        assertEquals(TEST_BYTE, response.get(EXCEL), BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationsNonSjp() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        Map<FileType, byte[]> response = publicationManagementService
            .getStoredPublications(TEST_ARTEFACT_ID, TEST, true);

        assertEquals(TEST_BYTE, response.get(PDF), BYTES_NO_MATCH);
        assertEquals(0, response.get(EXCEL).length, BYTES_NO_MATCH);
    }

    @Test
    void testGetStoredPublicationsAuthorisedPublic() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        ARTEFACT.setSensitivity(Sensitivity.PUBLIC);
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(azureBlobService.getBlobFile(any())).thenReturn(TEST_BYTE);

        Map<FileType, byte[]> response = publicationManagementService
            .getStoredPublications(TEST_ARTEFACT_ID, TEST, false);

        assertEquals(TEST_BYTE, response.get(PDF), BYTES_NO_MATCH);
        assertEquals(0, response.get(EXCEL).length, BYTES_NO_MATCH);
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

    @Test
    void testMaskDataSourceName() {
        ARTEFACT.setProvenance("SNL");
        String result = PublicationManagementService.maskDataSourceName(ARTEFACT.getProvenance());
        assertEquals("ListAssist", result, "Provenance should be changed to ListAssist");
    }

    @Test
    void testDoNotMaskDataSourceName() {
        ARTEFACT.setProvenance("MANUAL_UPLOAD");
        String result = PublicationManagementService.maskDataSourceName(ARTEFACT.getProvenance());
        assertEquals("MANUAL_UPLOAD", result, "Provenance should not be changed");
    }
}
