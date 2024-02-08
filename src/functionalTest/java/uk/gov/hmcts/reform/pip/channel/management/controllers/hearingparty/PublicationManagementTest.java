package uk.gov.hmcts.reform.pip.channel.management.controllers.hearingparty;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.pip.channel.management.Application;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.publication.FileType.PDF;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.TooManyMethods", "PMD.ExcessiveImports"})
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("functional")
@AutoConfigureMockMvc
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
class PublicationManagementTest {
    private static final String ROOT_URL = "/publication";
    private static final String V2_URL = "/v2";
    private static final String GET_ARTEFACT_SUMMARY = ROOT_URL + "/summary";
    private static final String INPUT_PARAMETERS = "parameters";
    private static final String ARTEFACT_ID_CROWN_DAILY_LIST = "3f8ac854-7d82-42cd-8e33-c31ee5442d36";
    private static final String ARTEFACT_ID_CROWN_FIRM_LIST = "84989c64-0ef6-4267-b405-4fb7255ae23d";
    private static final String ARTEFACT_ID_CROWN_WARNED_LIST = "85871ab3-8e53-422a-a3e6-e164c66e1683";
    private static final String ARTEFACT_ID_MAGISTRATES_PUBLIC_LIST = "b872b7e1-4a59-495e-a306-50c47f92e08f";
    private static final String CONTENT_MISMATCH_ERROR = "Artefact summary content should match";
    private static final String FILE_TYPE_HEADER = "x-file-type";
    private static final String SYSTEM_HEADER = "x-system";
    private static ObjectMapper objectMapper;
    private static MockMultipartFile file;
    @MockBean
    BlobContainerClient blobContainerClient;
    @MockBean
    BlobClient blobClient;
    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public static void setup() {
        file = new MockMultipartFile("file", "test.pdf",
                                     MediaType.APPLICATION_PDF_VALUE, "test content".getBytes(
            StandardCharsets.UTF_8)
        );

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of(ARTEFACT_ID_CROWN_DAILY_LIST), //Crown Daily List
            Arguments.of(ARTEFACT_ID_CROWN_FIRM_LIST), //Crown Firm List
            Arguments.of(ARTEFACT_ID_CROWN_WARNED_LIST), //Crown Warned List
            Arguments.of(ARTEFACT_ID_MAGISTRATES_PUBLIC_LIST) //Magistrates Public List
        );
    }

    @Test
    void testGenerateArtefactSummaryCrownDailyList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CROWN_DAILY_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Sitting at - 10:40am"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case Reference - 112233445"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Defendant Name(s) - Defendant_SN, Defendant_FN"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(
            "Hearing Type - FHDRA1 (First Hearing and Dispute Resolution Appointment)"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecuting Authority - Pro_Auth"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Listing Notes - Listing details text"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Sitting at - 10:40am"), CONTENT_MISMATCH_ERROR);
        assertTrue(
            responseContent.contains("Reporting Restriction - This is a reporting restriction detail, "
                                         + "This is another reporting restriction detail"),
            CONTENT_MISMATCH_ERROR
        );
    }

    @Test
    void testGenerateArtefactSummaryCrownFirmList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CROWN_FIRM_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Sitting at - 2:09pm"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case Reference - 12341234"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Defendant Name(s) - Surname, Forenames"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing Type - Directions"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Duration - 1 min [2 of 3]"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecuting Authority - Org name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Linked Cases - 1234"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCrownWarnedList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CROWN_WARNED_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Case Reference: 12341234"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Defendant Name(s): Surname, Forenames"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Fixed For: 03/03/2023"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecuting Authority: Org name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Linked Cases: 1234"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryMagistratesPublicList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_MAGISTRATES_PUBLIC_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Sitting at - 10:40am"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case Reference - 12345678"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Defendant Name(s) - Defendant_SN, Defendant_FN"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(
            "Hearing Type - FHDRA1 (First Hearing and Dispute Resolution Appointment)"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecuting Authority - Pro_Auth"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Duration - 1 hour 5 mins [2 of 3]"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case Details - Listing details text"), CONTENT_MISMATCH_ERROR);
    }

    @ParameterizedTest
    @MethodSource(INPUT_PARAMETERS)
    void testGetFileOK(String listArtefactId) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MvcResult response = mockMvc.perform(
                get(ROOT_URL + V2_URL + "/" + listArtefactId)
                    .header(SYSTEM_HEADER, "true")
                    .header(FILE_TYPE_HEADER, PDF)
                    .param("maxFileSize", "2048000"))

            .andExpect(status().isOk()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Response should not be null"
        );
        byte[] decodedBytes = Base64.getDecoder().decode(response.getResponse().getContentAsString());
        String decodedResponse = new String(decodedBytes);

        assertTrue(
            decodedResponse.contains("test content"),
            "Response does not contain expected result"
        );
    }
}
