package uk.gov.hmcts.reform.pip.channel.management.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.channel.management.models.PublicationFileSizes;
import uk.gov.hmcts.reform.pip.channel.management.services.PublicationManagementService;
import uk.gov.hmcts.reform.pip.model.publication.FileType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationManagementControllerTest {
    private static final String FILE = "123";
    private static final String USER_ID = "test";
    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final ListType LIST_TYPE = ListType.SJP_PUBLIC_LIST;
    private static final Language LANGUAGE = Language.ENGLISH;

    private static final String STATUS_MESSAGE = "Status did not match";
    private static final String RESPONSE_BODY_MESSAGE = "Body did not match";

    @Mock
    private PublicationManagementService publicationManagementService;

    @InjectMocks
    PublicationManagementController publicationManagementController;

    @Test
    void testGenerateArtefactSummary() {
        when(publicationManagementService.generateArtefactSummary(any())).thenReturn("test1234");
        ResponseEntity<String> response = publicationManagementController
            .generateArtefactSummary(UUID.randomUUID());

        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_MESSAGE);
        assertEquals("test1234", response.getBody(), RESPONSE_BODY_MESSAGE);
    }

    @Test
    void testGenerateFiles() {
        ResponseEntity<Void> response = publicationManagementController
            .generateFiles(UUID.randomUUID());

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode(), STATUS_MESSAGE);
    }

    @Test
    void testGenerateFilesV2() {
        ResponseEntity<Void> response = publicationManagementController
            .generateFiles(UUID.randomUUID(), "payload");

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode(), STATUS_MESSAGE);
    }

    @Test
    void testGetFile() {
        when(publicationManagementService.getStoredPublication(any(), any(), any(), eq(USER_ID), eq(true),
                                                               eq(false)
        )).thenReturn(FILE);

        ResponseEntity<String> response = publicationManagementController.getFile(
            UUID.randomUUID(), USER_ID, true, FileType.PDF, false, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_MESSAGE);
        assertEquals(FILE, response.getBody(), RESPONSE_BODY_MESSAGE);
    }

    @Test
    void testDeleteFilesV2() {
        doNothing().when(publicationManagementService).deleteFiles(ARTEFACT_ID, LIST_TYPE, LANGUAGE);

        ResponseEntity<Void> response = publicationManagementController.deleteFiles(ARTEFACT_ID, LIST_TYPE, LANGUAGE);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), STATUS_MESSAGE);
    }

    @Test
    void testFileExists() {
        when(publicationManagementService.fileExists(ARTEFACT_ID)).thenReturn(true);

        ResponseEntity<Boolean> response = publicationManagementController.fileExists(ARTEFACT_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_MESSAGE);
        assertEquals(true, response.getBody(), RESPONSE_BODY_MESSAGE);
    }

    @Test
    void testGetFileSizes() {
        PublicationFileSizes fileSizes = new PublicationFileSizes(1234L, null, 123L);
        when(publicationManagementService.getFileSizes(ARTEFACT_ID)).thenReturn(fileSizes);

        ResponseEntity<PublicationFileSizes> response = publicationManagementController.getFileSizes(ARTEFACT_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_MESSAGE);
        assertEquals(fileSizes, response.getBody(), RESPONSE_BODY_MESSAGE);
    }
}
