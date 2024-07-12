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
    private static final String ARTEFACT_ID_SSCS_DAILY_LIST = "1c96a1ca-3129-4e9b-aaeb-499ecd775e8c";
    private static final String ARTEFACT_ID_SSCS_DAILY_LIST_ADDITIONAL_HEARINGS
        = "a0071d36-af08-4638-a7b3-7ea65327b4dd";
    private static final String CONTENT_MISMATCH_ERROR = "Artefact summary content should match";
    private static final String FILE_TYPE_HEADER = "x-file-type";
    private static final String SYSTEM_HEADER = "x-system";
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

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of(ARTEFACT_ID_SSCS_DAILY_LIST), //SSCS Daily List
            Arguments.of(ARTEFACT_ID_SSCS_DAILY_LIST_ADDITIONAL_HEARINGS) //SSCS Daily List - Additional Hearings
        );
    }

    @Test
    void testGenerateArtefactSummaryCrownDailyList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CROWN_DAILY_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Defendant_SN, Defendant_FN"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - Pro_Auth"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - 112233445"), CONTENT_MISMATCH_ERROR);
        assertTrue(
            responseContent.contains("Hearing type - FHDRA1 (First Hearing and Dispute Resolution Appointment)"),
            CONTENT_MISMATCH_ERROR
        );
    }

    @Test
    void testGenerateArtefactSummaryCrownFirmList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CROWN_FIRM_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Surname, Forenames"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - Org name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - 12341234"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing type - Directions"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCrownWarnedList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CROWN_WARNED_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Surname, Forenames"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - Org name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - 12341234"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing date - 03/03/2023"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryMagistratesPublicList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_MAGISTRATES_PUBLIC_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant - Defendant_SN, Defendant_FN"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor - Pro_Auth"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case reference - 112233445"), CONTENT_MISMATCH_ERROR);
        assertTrue(
            responseContent.contains("Hearing type - FHDRA1 (First Hearing and Dispute Resolution Appointment)"),
            CONTENT_MISMATCH_ERROR
        );
    }

    @Test
    void testGenerateArtefactSummarySscsDailyList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_SSCS_DAILY_LIST))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Appellant - Surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Respondent - Respondent Organisation, Respondent Organisation 2"),
                   CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Appeal reference - 12341235"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummarySscsDailyListAdditionalHearings() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_SSCS_DAILY_LIST_ADDITIONAL_HEARINGS))
            .andExpect(status().isOk()).andReturn();

        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Appellant - Surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Respondent - Respondent Organisation, Respondent Organisation 2"),
                   CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Appeal reference - 12341235"), CONTENT_MISMATCH_ERROR);
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
