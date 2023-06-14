package uk.gov.hmcts.reform.pip.channel.management.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.channel.management.services.PublicationManagementService;
import uk.gov.hmcts.reform.pip.model.publication.FileType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationManagementControllerTest {
    private static final String FILE = "123";
    private static final String USER_ID = "test";
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
    void testGetFile() {
        when(publicationManagementService.getStoredPublication(any(), any(), any(), eq(USER_ID), eq(true)))
            .thenReturn(FILE);

        ResponseEntity<String> response = publicationManagementController.getFile(
            UUID.randomUUID(), USER_ID, true, FileType.PDF, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_MESSAGE);
        assertEquals(FILE, response.getBody(), RESPONSE_BODY_MESSAGE);
    }

    @Test
    void testGetFiles() {
        Map<FileType, byte[]> testMap = new ConcurrentHashMap<>();
        testMap.put(FileType.PDF, new byte[100]);
        testMap.put(FileType.EXCEL, new byte[0]);
        when(publicationManagementService.getStoredPublications(any(), eq(USER_ID), eq(true))).thenReturn(testMap);

        ResponseEntity<Map<FileType, byte[]>> response = publicationManagementController
            .getFiles(UUID.randomUUID(), USER_ID, true);

        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_MESSAGE);
        assertEquals(testMap, response.getBody(), RESPONSE_BODY_MESSAGE);
    }
}
