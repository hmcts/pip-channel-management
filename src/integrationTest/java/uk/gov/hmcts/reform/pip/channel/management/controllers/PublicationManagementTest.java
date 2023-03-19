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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
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
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
class PublicationManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    BlobContainerClient blobContainerClient;

    @Autowired
    BlobClient blobClient;

    @Value("${VERIFIED_USER_ID}")
    private String verifiedUserId;

    private static ObjectMapper objectMapper;

    private static final String ROOT_URL = "/publication";
    private static final String GET_ARTEFACT_SUMMARY = ROOT_URL + "/summary";
    private static final String ARTEFACT_ID = "3d498688-bbad-4a53-b253-a16ddf8737a9";
    private static final String ARTEFACT_ID_NOT_FOUND = "11111111-1111-1111-1111-111111111111";
    private static final String INPUT_PARAMETERS = "parameters";
    private static MockMultipartFile file;

    @BeforeAll
    public static void setup() {
        file = new MockMultipartFile("file", "test.pdf",
                                     MediaType.APPLICATION_PDF_VALUE, "test content".getBytes(
            StandardCharsets.UTF_8)
        );

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of("5697562a-1b96-4386-8bde-355849025c57"), //Care Standards Tribunal Hearing List
            Arguments.of("aa7376b7-7a67-4446-b51e-f5718a54ec72"), //Civil and Family Daily Cause List
            Arguments.of("a1464fc0-9dc7-4721-a59b-2d870d6f5c35"), //Civil Daily Cause List
            Arguments.of("03dafc5e-6264-4ade-ba94-6aebd0ac23ba"), //Court of Protection Daily Cause List
            Arguments.of("3f8ac854-7d82-42cd-8e33-c31ee5442d36"), //Crown Daily List
            Arguments.of("cd93565d-a3ab-4da2-a0aa-37433227e7de"), //Crown Firm List
            Arguments.of("85871ab3-8e53-422a-a3e6-e164c66e1683"), //Crown Warned List
            Arguments.of("10b40fa9-47b1-4a12-85e0-d8be67d8eaf5"), //Employment Tribunals Daily List
            Arguments.of("b9d5a447-29db-4025-8326-4413ec240e1a"), //Employment Tribunals Fortnightly Press List
            Arguments.of("63c3d528-5e33-4067-ae54-eac2eee9f645"), //Family Daily Cause List
            Arguments.of("aa5e97d3-b82a-436a-9621-8b0fb2a987ca"), //Immigration and Asylum Chamber Daily List
            Arguments.of("93d9600c-af8b-44fa-ac5c-c8419933d185"), //Magistrates Public List
            Arguments.of("af7c6ba8-c391-458f-9246-40f419a98a12"), //Magistrates Standard List
            Arguments.of("e646650b-c7dc-4551-9163-f0f792b83e54"), //Primary Health Tribunal Hearing List
            Arguments.of("5dea6753-7a1d-4b91-b3c7-06721e3332cd"), //Single Justice Procedure Press List
            Arguments.of("3d498688-bbad-4a53-b253-a16ddf8737a9"), //Single Justice Procedure Public List
            Arguments.of("a954f6f1-fc82-403b-9a01-4bb11578f08a"), //SSCS Daily List,
            Arguments.of("c21bf262-d0b5-475e-b0e3-12aa34495469")  //SSCS Daily List - Additional Hearings
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

        assertEquals(
            exceptionResponse.getMessage(),
            "Artefact with id " + ARTEFACT_ID_NOT_FOUND + " not found",
            "Unable to send NotFound exception"
        );
    }

    @Test
    @WithMockUser(username = "unknown_user", authorities = {"APPROLE_api.request.unknown"})
    void testGenerateArtefactSummaryUnauthorized() throws Exception {
        mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID))
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource(INPUT_PARAMETERS)
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

        assertEquals(
            exceptionResponse.getMessage(),
            "Artefact with id " + ARTEFACT_ID_NOT_FOUND + " not found",
            "Unable to send NotFound exception"
        );
    }

    @Test
    @WithMockUser(username = "unknown_user", authorities = {"APPROLE_api.request.unknown"})
    void testGenerateFileUnauthorized() throws Exception {
        mockMvc.perform(post(ROOT_URL + "/" + ARTEFACT_ID))
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource(INPUT_PARAMETERS)
    void testGetFilesOK(String listArtefactId) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MvcResult response = mockMvc.perform(
                get(ROOT_URL + "/" + listArtefactId)
                    .header("x-system", "true"))
            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Response should contain a Artefact"
        );
        assertTrue(
            response.getResponse().getContentAsString().contains("PDF"),
            "Response does not contain PDF information"
        );
        assertTrue(
            response.getResponse().getContentAsString().contains("EXCEL"),
            "Response does not contain excel"
        );
    }

    @ParameterizedTest
    @MethodSource(INPUT_PARAMETERS)
    void testGetFilesForUserId(String listArtefactId) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MockHttpServletRequestBuilder request =
            get(ROOT_URL + "/" + listArtefactId)
                .header("x-user-id", verifiedUserId)
                .header("x-system", "false");

        MvcResult response = mockMvc.perform(request)
            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Response should contain a Artefact"
        );
        assertTrue(
            response.getResponse().getContentAsString().contains("PDF"),
            "Response does not contain PDF information"
        );
        assertTrue(
            response.getResponse().getContentAsString().contains("EXCEL"),
            "Response does not contain excel"
        );
    }

    @Test
    void testGetFilesNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(ROOT_URL + "/" + ARTEFACT_ID_NOT_FOUND))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(
            exceptionResponse.getMessage(),
            "Artefact with id " + ARTEFACT_ID_NOT_FOUND + " not found",
            "Unable to send NotFound exception"
        );
    }

    @Test
    @WithMockUser(username = "unknown_user", authorities = {"APPROLE_api.request.unknown"})
    void testGetFilesUnauthorized() throws Exception {
        mockMvc.perform(get(ROOT_URL + "/" + ARTEFACT_ID))
            .andExpect(status().isForbidden());
    }
}
