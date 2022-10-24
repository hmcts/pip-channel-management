package uk.gov.hmcts.reform.pip.channel.management.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.channel.management.services.PublicationManagementService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationManagementControllerTest {

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
    void testGetFiles() {
        Map<String, byte[]> testMap = new ConcurrentHashMap<>();
        testMap.put("PDF", new byte[100]);
        testMap.put("EXCEL", new byte[0]);
        when(publicationManagementService.getStoredPublications(any())).thenReturn(testMap);

        ResponseEntity<Map<String, byte[]>> response = publicationManagementController
            .getFiles(UUID.randomUUID());

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status did not match");
        assertEquals(testMap, response.getBody(), "Body did not match");
    }
}
