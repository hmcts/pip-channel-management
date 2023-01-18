package uk.gov.hmcts.reform.pip.channel.management.controllers;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.pip.channel.management.Application;
import uk.gov.hmcts.reform.pip.channel.management.errorhandling.ExceptionResponse;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@AutoConfigureMockMvc
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin" })
class PublicationManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    BlobContainerClient blobContainerClient;

    @Autowired
    BlobClient blobClient;

    private static ObjectMapper objectMapper;

    private static final String ROOT_URL = "/publication";
    private static final String GET_ARTEFACT_SUMMARY = ROOT_URL + "/summary";
    private static final String ARTEFACT_ID = "591d5021-bf40-4066-b65e-da3221060a54";
    private static final String ARTEFACT_ID_NOT_FOUND = "11111111-1111-1111-1111-111111111111";
    private static MockMultipartFile file;

    @BeforeAll
    public static void setup() {
        file = new MockMultipartFile("file", "test.pdf",
                                     MediaType.APPLICATION_PDF_VALUE, "test content".getBytes(
            StandardCharsets.UTF_8));

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testGenerateArtefactSummaryOK() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID))
            .andExpect(status().isOk()).andReturn();
        assertThat(response.getResponse().getContentAsString()).contains("Preston court");
    }

    @Test
    void testGenerateArtefactSummaryNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_NOT_FOUND))
            .andExpect(status().isNotFound()).andReturn();

        ExceptionResponse exceptionResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(exceptionResponse.getMessage(),
                     "Artefact with id " + ARTEFACT_ID_NOT_FOUND + " not found",
                     "Unable to send NotFound exception");
    }

    @Test
    @WithMockUser(username = "unknown_user", authorities = {"APPROLE_api.request.unknown"})
    void testGenerateArtefactSummaryUnauthorized() throws Exception {
        mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGenerateFileAccepted() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);

        mockMvc.perform(post(ROOT_URL + "/" + ARTEFACT_ID))
            .andExpect(status().isAccepted()).andReturn();
    }

    @Test
    void testGenerateFileNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(ROOT_URL + "/" + ARTEFACT_ID_NOT_FOUND))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(exceptionResponse.getMessage(),
                     "Artefact with id " + ARTEFACT_ID_NOT_FOUND + " not found",
                     "Unable to send NotFound exception");
    }

    @Test
    @WithMockUser(username = "unknown_user", authorities = {"APPROLE_api.request.unknown"})
    void testGenerateFileUnauthorized() throws Exception {
        mockMvc.perform(post(ROOT_URL + "/" + ARTEFACT_ID))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGetFilesOK() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MvcResult response = mockMvc.perform(get(ROOT_URL + "/" + ARTEFACT_ID))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(),
                      "Response should contain a Artefact");
        assertTrue(response.getResponse().getContentAsString().contains("PDF"),
                   "Response does not contain PDF information");
        assertTrue(response.getResponse().getContentAsString().contains("\"EXCEL\":\"\""),
                   "Response does not contain empty excel");
    }

    @Test
    void testGetFilesNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(ROOT_URL + "/" + ARTEFACT_ID_NOT_FOUND))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(exceptionResponse.getMessage(),
                     "Artefact with id " + ARTEFACT_ID_NOT_FOUND + " not found",
                     "Unable to send NotFound exception");
    }

    @Test
    @WithMockUser(username = "unknown_user", authorities = {"APPROLE_api.request.unknown"})
    void testGetFilesUnauthorized() throws Exception {
        mockMvc.perform(get(ROOT_URL + "/" + ARTEFACT_ID))
            .andExpect(status().isForbidden());
    }
}
