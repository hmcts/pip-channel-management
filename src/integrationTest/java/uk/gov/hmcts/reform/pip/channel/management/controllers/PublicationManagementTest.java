package uk.gov.hmcts.reform.pip.channel.management.controllers;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.TooManyMethods"})
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

    private static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of("567af80f-a702-449d-b106-20661493ef7a"), //Care Standards Tribunal Hearing List
            Arguments.of("cf5d8220-bbbe-4de4-9ca5-e0bee354f49b"), //Civil and Family Daily Cause List
            Arguments.of("0bef43e6-8987-411d-9411-26167730e88c"), //Civil Daily Cause List
            Arguments.of("79e801f1-a746-4640-861b-0f964951a733"), //Court of Protection Daily Cause List
            //TODO: Pdf generation is failing, we need to fix the bug PUB-1801
            //Arguments.of("ef94897d-ea6a-41b5-8948-aea4a99d9475"), //Crown Daily List
            Arguments.of("a6046b89-0ce4-49d7-8036-6144924249a0"), //Crown Firm List
            Arguments.of("7aad9d44-fc2b-43a8-a93b-a46a62589ecf"), //Crown Warned List
            Arguments.of("87f18f00-3543-4a52-a197-b3cf537c4eb0"), //Employment Tribunals Daily List
            Arguments.of("58fafa97-e50c-45a3-b5bc-9e66ca64c3f7"), //Employment Tribunals Fortnightly Press List
            Arguments.of("0136c44e-96da-4737-a524-5094511fb1ad"), //Family Daily Cause List
            Arguments.of("330bef3e-0d8a-4a59-a534-527dc37f94d8"), //Immigration and Asylum Chamber Daily List
            Arguments.of("bdf23b7e-0063-4d6b-ab74-bf52af734914"), //Magistrates Public List
            Arguments.of("e37a9f48-513c-42e0-b9ea-e8e5cb157966"), //Magistrates Standard List
            Arguments.of("c4ca592c-2814-4de5-84b7-ecc5d15ce833"), //Primary Health Tribunal Hearing List
            Arguments.of("5874fca9-28dd-4819-a2b7-639f211ef273"), //Single Justice Procedure Press List
            Arguments.of("9b093e03-e3ce-43a4-83e0-9eef6984a964"), //Single Justice Procedure Public List
            Arguments.of("1167228d-d62f-49a2-9361-8627482fb56e") //Social Security and Child Support Tribunal Daily List
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void testGenerateArtefactSummaryOK(String listArtefactId) throws Exception {
        mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + listArtefactId))
            .andExpect(status().isOk()).andReturn();
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

    @ParameterizedTest
    @MethodSource("parameters")
    void testGenerateFileAccepted(String listArtefactId) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);

        mockMvc.perform(post(ROOT_URL + "/" + listArtefactId))
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

    @ParameterizedTest
    @MethodSource("parameters")
    void testGetFilesOK(String listArtefactId) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MvcResult response = mockMvc.perform(get(ROOT_URL + "/" + listArtefactId))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(),
                      "Response should contain a Artefact");
        assertTrue(response.getResponse().getContentAsString().contains("PDF"),
                   "Response does not contain PDF information");
        assertTrue(response.getResponse().getContentAsString().contains("EXCEL"),
                   "Response does not contain excel");
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
