package uk.gov.hmcts.reform.pip.channel.management.controllers;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
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
import uk.gov.hmcts.reform.pip.model.publication.FileType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.TooManyMethods", "PMD.ExcessiveImports"})
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("functional")
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

    private static final String ROOT_URL = "/publication";
    private static final String V2_URL = "/v2";
    private static final String GET_ARTEFACT_SUMMARY = ROOT_URL + "/summary";
    private static final String ARTEFACT_ID = "3d498688-bbad-4a53-b253-a16ddf8737a9";
    private static final String ARTEFACT_ID_NOT_FOUND = "11111111-1111-1111-1111-111111111111";
    private static final String INPUT_PARAMETERS = "parameters";
    private static final String ARTEFACT_NOT_FOUND_MESSAGE = "Artefact with id %s not found";
    private static final String NOT_FOUND_RESPONSE_MESSAGE = "Artefact not found message does not match";
    private static final String ARTEFACT_ID_CARE_STANDARDS_LIST = "5697562a-1b96-4386-8bde-355849025c57";
    private static final String ARTEFACT_ID_CIVIL_AND_FAMILY_DAILY_CAUSE_LIST = "aa7376b7-7a67-4446-b51e-f5718a54ec72";
    private static final String ARTEFACT_ID_CIVIL_DAILY_CAUSE_LIST = "a1464fc0-9dc7-4721-a59b-2d870d6f5c35";
    private static final String ARTEFACT_ID_COP_DAILY_CAUSE_LIST = "2e8d48ad-2290-4383-b263-dd7ce328fa0a";
    private static final String ARTEFACT_ID_CROWN_DAILY_LIST = "3f8ac854-7d82-42cd-8e33-c31ee5442d36";
    private static final String ARTEFACT_ID_CROWN_FIRM_LIST = "cd93565d-a3ab-4da2-a0aa-37433227e7de";
    private static final String ARTEFACT_ID_CROWN_WARNED_LIST = "85871ab3-8e53-422a-a3e6-e164c66e1683";
    private static final String ARTEFACT_ID_ET_DAILY_LIST = "10b40fa9-47b1-4a12-85e0-d8be67d8eaf5";
    private static final String ARTEFACT_ID_ET_FORTNIGHTLY_PRESS_LIST = "b9d5a447-29db-4025-8326-4413ec240e1a";
    private static final String ARTEFACT_ID_FAMILY_DAILY_CAUSE_LIST = "63c3d528-5e33-4067-ae54-eac2eee9f645";
    private static final String ARTEFACT_ID_IAC_DAILY_LIST = "aa5e97d3-b82a-436a-9621-8b0fb2a987ca";
    private static final String ARTEFACT_ID_MAGISTRATES_PUBLIC_LIST = "93d9600c-af8b-44fa-ac5c-c8419933d185";
    private static final String ARTEFACT_ID_MAGISTRATES_STANDARD_LIST = "af7c6ba8-c391-458f-9246-40f419a98a12";
    private static final String ARTEFACT_ID_PRIMARY_HEALTH_LIST = "e646650b-c7dc-4551-9163-f0f792b83e54";
    private static final String ARTEFACT_ID_SJP_PRESS_LIST = "5dea6753-7a1d-4b91-b3c7-06721e3332cd";
    private static final String ARTEFACT_ID_SJP_PUBLIC_LIST = "3d498688-bbad-4a53-b253-a16ddf8737a9";
    private static final String ARTEFACT_ID_SSCS_DAILY_LIST = "a954f6f1-fc82-403b-9a01-4bb11578f08a";
    private static final String ARTEFACT_ID_SSCS_DAILY_LIST_ADDITIONAL_HEARINGS
        = "c21bf262-d0b5-475e-b0e3-12aa34495469";
    private static final String CONTENT_MISMATCH_ERROR = "Artefact summary content should match";
    private static final String FILE_TYPE_HEADER = "x-file-type";
    private static final String UNAUTHORIZED_USERNAME = "unauthorized_username";
    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.role";
    private static final String SYSTEM_HEADER = "x-system";

    private static ObjectMapper objectMapper;
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

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of(ARTEFACT_ID_CARE_STANDARDS_LIST), //Care Standards Tribunal Hearing List
            Arguments.of(ARTEFACT_ID_CIVIL_AND_FAMILY_DAILY_CAUSE_LIST), //Civil and Family Daily Cause List
            Arguments.of(ARTEFACT_ID_CIVIL_DAILY_CAUSE_LIST), //Civil Daily Cause List
            Arguments.of(ARTEFACT_ID_COP_DAILY_CAUSE_LIST), //Court of Protection Daily Cause List
            Arguments.of(ARTEFACT_ID_CROWN_DAILY_LIST), //Crown Daily List
            Arguments.of(ARTEFACT_ID_CROWN_FIRM_LIST), //Crown Firm List
            Arguments.of(ARTEFACT_ID_CROWN_WARNED_LIST), //Crown Warned List
            Arguments.of(ARTEFACT_ID_ET_DAILY_LIST), //Employment Tribunals Daily List
            Arguments.of(ARTEFACT_ID_ET_FORTNIGHTLY_PRESS_LIST), //Employment Tribunals Fortnightly Press List
            Arguments.of(ARTEFACT_ID_FAMILY_DAILY_CAUSE_LIST), //Family Daily Cause List
            Arguments.of(ARTEFACT_ID_IAC_DAILY_LIST), //Immigration and Asylum Chamber Daily List
            Arguments.of(ARTEFACT_ID_MAGISTRATES_PUBLIC_LIST), //Magistrates Public List
            Arguments.of(ARTEFACT_ID_MAGISTRATES_STANDARD_LIST), //Magistrates Standard List
            Arguments.of(ARTEFACT_ID_PRIMARY_HEALTH_LIST), //Primary Health Tribunal Hearing List
            Arguments.of(ARTEFACT_ID_SJP_PRESS_LIST), //Single Justice Procedure Press List
            Arguments.of(ARTEFACT_ID_SJP_PUBLIC_LIST), //Single Justice Procedure Public List
            Arguments.of(ARTEFACT_ID_SSCS_DAILY_LIST), //SSCS Daily List,
            Arguments.of(ARTEFACT_ID_SSCS_DAILY_LIST_ADDITIONAL_HEARINGS)  //SSCS Daily List - Additional Hearings
        );
    }

    @Test
    void testGenerateArtefactSummaryCareStandardsList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CARE_STANDARDS_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Hearing Date: 04 October"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case Name: A Vs B"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing Type: Remote - Teams"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Venue: PRESTON, Address Line 1, AA1 AA1"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCivilAndFamilyDailyCauseList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CIVIL_AND_FAMILY_DAILY_CAUSE_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Case Name - A1 Vs B1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case ID - 12345678"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing Type - FMPO"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Location - testSittingChannel"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Duration - 1 hour 5 mins"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Judge - 1: Before: Presiding"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCivilDailyCauseList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_CIVIL_DAILY_CAUSE_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Courtroom: 1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Judiciary: Mr , Mr"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case Name: A1 Vs B1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case Reference: 12345678"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing Type: FMPO"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Start Time: 9:40am"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing Channel: testSittingChannel"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryCourtOfProtectionDailyCauseList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_COP_DAILY_CAUSE_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Name of Party(ies) - ThisIsACaseSuppressionName"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case ID - 12341234"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing Type - Criminal"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Location - Teams, In-Person"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Duration - 1 hour [1 of 2]"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Before - Crown Judge"), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains("Duration - 1 min [[2 of 3]]"), CONTENT_MISMATCH_ERROR);
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
    void testGenerateArtefactSummaryEmploymentTribunalsDailyList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_ET_DAILY_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Case Number: 12341234"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Claimant: , Rep: Mr T Test Surname 2"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing Type: This is a hearing type"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Jurisdiction: This is a case type"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing Platform: This is a sitting channel"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryEmploymentTribunalsFortnightlyPressList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_ET_FORTNIGHTLY_PRESS_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Courtroom - Court 1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Start Time - 9:30am"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Duration - 2 hours [[2 of 3]]"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case Number - 12341234"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Claimant - , Rep: Mr T Test Surname 2"), CONTENT_MISMATCH_ERROR);
        assertTrue(
            responseContent
                .contains("Respondent - Capt. T Test Surname, Rep: Dr T Test Surname 2"),
            CONTENT_MISMATCH_ERROR
        );
        assertTrue(responseContent.contains("Hearing Type - This is a hearing type"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Jurisdiction - This is a case type"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("earing Platform - This is a sitting channel"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryFamilyDailyCauseList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_FAMILY_DAILY_CAUSE_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Case Name - A2 Vs B2"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case ID - 112233445"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(
            "Hearing Type - FHDRA1 (First Hearing and Dispute Resolution Appointment)"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Location - testSittingChannel"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Duration - 1 hour 5 mins"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Judge - 1: Before: Presiding"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryImmigrationAndAsylumChamberDailyList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_IAC_DAILY_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Start Time - 2:00pm"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case Ref - 12341234"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing Channel - Teams, Attended"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Appellant - Surname"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecuting Authority - Surname"), CONTENT_MISMATCH_ERROR);
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
        assertTrue(responseContent.contains("Duration - 1 hour 5 mins"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case Details - Listing details text"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryMagistratesStandardList() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_MAGISTRATES_STANDARD_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant Name - Surname1, Forename1 (male)"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("DOB and Age - 01/01/1983 Age: 39"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains(
            "Defendant Address - Address Line 1, Address Line 2, Month A, County A, AA1 AA1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecuting Authority - Test1234"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing Number - 12"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Attendance Method - VIDEO HEARING"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case Ref - 45684548"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing of Type - mda"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Panel - ADULT"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("1. drink driving"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Plea - NOT_GUILTY"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("driving whilst under the influence of alcohol"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("2. Assault by beating"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Plea - NOT_GUILTY"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Assault by beating"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryPrimaryHealthTribunalHearingList() throws Exception {
        MvcResult response = mockMvc
            .perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_PRIMARY_HEALTH_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Hearing Date: 04 October"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Case Name: A Vs B"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Duration: 1 day"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Hearing Type: Remote - Teams"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Venue: PRESTON, Address Line 1, AA1 AA1"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummarySingleJusticeProcedurePressList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_SJP_PRESS_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains(
            "Accused: This is a title This is a forename This is a middle name This is a surname"),
                   CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Postcode: AA1 AA1"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor: This is an organisation"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent
                       .contains("Offence: This is an offence title (Reporting restriction)"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummarySingleJusticeProcedurePublicList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_SJP_PUBLIC_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Defendant: Z CDFake"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Postcode: BD17"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Prosecutor: TV Licensing"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent
                       .contains("Offence: Use / install a television set without a licence"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummarySscsDailyList() throws Exception {
        MvcResult response = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_SSCS_DAILY_LIST))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(
            responseContent.contains(
                "Appellant: Surname, Legal Advisor: Mr Individual Forenames Individual Middlename Individual Surname"),
            CONTENT_MISMATCH_ERROR
        );
        assertTrue(responseContent.contains("Prosecutor: test, test2"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Panel: Judge Test Name, Magistrate Test Name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Tribunal type: Teams, Attended"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummarySscsDailyListAdditionalHearings() throws Exception {
        MvcResult response = mockMvc.perform(
                get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_SSCS_DAILY_LIST_ADDITIONAL_HEARINGS))
            .andExpect(status().isOk()).andReturn();
        String responseContent = response.getResponse().getContentAsString();
        assertTrue(
            responseContent.contains(
                "Appellant: Surname, Legal Advisor: Mr Individual Forenames Individual Middlename Individual Surname"),
            CONTENT_MISMATCH_ERROR
        );
        assertTrue(responseContent.contains("Prosecutor: test, test2"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Panel: Judge Test Name, Magistrate Test Name"), CONTENT_MISMATCH_ERROR);
        assertTrue(responseContent.contains("Tribunal type: Teams, Attended"), CONTENT_MISMATCH_ERROR);
    }

    @Test
    void testGenerateArtefactSummaryNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(GET_ARTEFACT_SUMMARY + "/" + ARTEFACT_ID_NOT_FOUND))
            .andExpect(status().isNotFound()).andReturn();

        ExceptionResponse exceptionResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(
            exceptionResponse.getMessage(),
            String.format(ARTEFACT_NOT_FOUND_MESSAGE, ARTEFACT_ID_NOT_FOUND),
            NOT_FOUND_RESPONSE_MESSAGE
        );
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
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
            String.format(ARTEFACT_NOT_FOUND_MESSAGE, ARTEFACT_ID_NOT_FOUND),
            NOT_FOUND_RESPONSE_MESSAGE
        );
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testGenerateFileUnauthorized() throws Exception {
        mockMvc.perform(post(ROOT_URL + "/" + ARTEFACT_ID))
            .andExpect(status().isForbidden());
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
                    .header(FILE_TYPE_HEADER, FileType.PDF)
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

    @ParameterizedTest
    @MethodSource(INPUT_PARAMETERS)
    void testGetFileForUserId(String listArtefactId) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MockHttpServletRequestBuilder request =
            get(ROOT_URL + V2_URL + "/" + listArtefactId)
                .header("x-user-id", verifiedUserId)
                .header(SYSTEM_HEADER, "false")
                .header(FILE_TYPE_HEADER, FileType.PDF)
                .param("maxFileSize", "2048000");

        MvcResult response = mockMvc.perform(request)
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

    @ParameterizedTest
    @MethodSource(INPUT_PARAMETERS)
    void testGetFileSizeTooLarge(String listArtefactId) throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(
            BinaryData.fromString(new String(file.getBytes())));

        MockHttpServletRequestBuilder request =
            get(ROOT_URL + V2_URL + "/" + listArtefactId)
                .header("x-user-id", verifiedUserId)
                .header(SYSTEM_HEADER, "false")
                .header(FILE_TYPE_HEADER, FileType.PDF)
                .param("maxFileSize", "10");

        MvcResult response = mockMvc.perform(request)
            .andExpect(status().isPayloadTooLarge()).andReturn();

        assertNotNull(
            response.getResponse().getContentAsString(),
            "Response should not be null"
        );

        assertTrue(
            response.getResponse().getContentAsString().contains("File with type PDF for artefact with id "
                                        + listArtefactId + " has size over the limit of 10 bytes"),
            "Response does not contain expected result"
        );
    }

    @Test
    void testGetFileNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(ROOT_URL + V2_URL + "/" + ARTEFACT_ID_NOT_FOUND)
                                                  .header(FILE_TYPE_HEADER, FileType.PDF))
            .andExpect(status().isNotFound())
            .andReturn();

        ExceptionResponse exceptionResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(
            exceptionResponse.getMessage(),
            String.format(ARTEFACT_NOT_FOUND_MESSAGE, ARTEFACT_ID_NOT_FOUND),
            NOT_FOUND_RESPONSE_MESSAGE
        );
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testGetFileUnauthorized() throws Exception {
        mockMvc.perform(get(ROOT_URL + V2_URL + "/" + ARTEFACT_ID)
                            .header(FILE_TYPE_HEADER, FileType.PDF))
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
                    .header(SYSTEM_HEADER, "true"))
            .andExpect(status().isOk()).andReturn();

        Assertions.assertNotNull(
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
                .header(SYSTEM_HEADER, "false");

        MvcResult response = mockMvc.perform(request)
            .andExpect(status().isOk()).andReturn();

        Assertions.assertNotNull(
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
            String.format(ARTEFACT_NOT_FOUND_MESSAGE, ARTEFACT_ID_NOT_FOUND),
            NOT_FOUND_RESPONSE_MESSAGE
        );
    }

    @Test
    @WithMockUser(username = "unknown_user", authorities = {"APPROLE_api.request.unknown"})
    void testGetFilesUnauthorized() throws Exception {
        mockMvc.perform(get(ROOT_URL + "/" + ARTEFACT_ID))
            .andExpect(status().isForbidden());
    }

}
