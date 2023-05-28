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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationManagementControllerTest {
    private static final String FILE = "123";

    @Mock
    private PublicationManagementService publicationManagementService;

    @InjectMocks
    PublicationManagementController publicationManagementController;

    @Test
    void testGenerateArtefactSummary() {
        when(publicationManagementService.generateArtefactSummary(any())).thenReturn("test1234");
        ResponseEntity<String> response = publicationManagementController
            .generateArtefactSummary(UUID.randomUUID());

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status did not match");
        assertEquals("test1234", response.getBody(), "Body did not match");
    }

    @Test
    void testGenerateFiles() {
        ResponseEntity<Void> response = publicationManagementController
            .generateFiles(UUID.randomUUID());

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode(), "Status did not match");
    }

    @Test
    void testGetFile() {
        when(publicationManagementService.getStoredPublication(any(), any(), any(), eq("test"), eq(true)))
            .thenReturn(FILE);

        ResponseEntity<String> response = publicationManagementController.getFile(
            UUID.randomUUID(), "test", true, FileType.PDF, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status did not match");
        assertEquals(FILE, response.getBody(), "Body did not match");
    }
}
