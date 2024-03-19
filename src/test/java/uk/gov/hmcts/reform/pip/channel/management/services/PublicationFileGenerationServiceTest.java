package uk.gov.hmcts.reform.pip.channel.management.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.Application;
import uk.gov.hmcts.reform.pip.channel.management.models.PublicationFiles;
import uk.gov.hmcts.reform.pip.model.location.Location;
import uk.gov.hmcts.reform.pip.model.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles(profiles = "test")
@SpringBootTest(classes = {Application.class})
class PublicationFileGenerationServiceTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Artefact ARTEFACT = new Artefact();
    private static final Artefact WELSH_ARTEFACT = new Artefact();
    private static final Location LOCATION = new Location();

    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final String LOCATION_ID = "1";

    private static final String FILE_PRESENT_MESSAGE = "Files should be present";
    private static final String FILE_NOT_PRESENT_MESSAGE = "Files should not be present";
    private static final String FILE_EMPTY_MESSAGE = "File should be empty";
    private static final String FILE_NOT_EMPTY_MESSAGE = "File should not be empty";

    private static String sjpPublicListInput;
    private static String civilDailyListInput;

    @MockBean
    private DataManagementService dataManagementService;

    @Autowired
    private PublicationFileGenerationService publicationFileGenerationService;

    @BeforeAll
    static void startup() throws IOException {
        OBJECT_MAPPER.findAndRegisterModules();
        sjpPublicListInput = getInput("/mocks/sjpPublicList.json");
        civilDailyListInput = getInput("/mocks/civilDailyCauseList.json");
    }

    private static String getInput(String resourcePath) throws IOException {
        try (InputStream inputStream = PublicationManagementServiceTest.class.getResourceAsStream(resourcePath)) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        }
    }

    @BeforeEach
    void setup() {
        ARTEFACT.setArtefactId(ARTEFACT_ID);
        ARTEFACT.setContentDate(LocalDateTime.now());
        ARTEFACT.setLocationId(LOCATION_ID);
        ARTEFACT.setProvenance("france");
        ARTEFACT.setLanguage(Language.ENGLISH);
        ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);

        WELSH_ARTEFACT.setArtefactId(ARTEFACT_ID);
        WELSH_ARTEFACT.setContentDate(LocalDateTime.now());
        WELSH_ARTEFACT.setLocationId(LOCATION_ID);
        WELSH_ARTEFACT.setProvenance("france");
        WELSH_ARTEFACT.setLanguage(Language.WELSH);
        WELSH_ARTEFACT.setListType(ListType.SJP_PUBLIC_LIST);

        LOCATION.setLocationId(Integer.valueOf(LOCATION_ID));
        LOCATION.setName("Test");
        LOCATION.setWelshName("Test");
        LOCATION.setRegion(Collections.singletonList("Test"));
    }

    @Test
    void testGenerateFilesSjpEnglish() {
        when(dataManagementService.getArtefact(ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(dataManagementService.getLocation(LOCATION_ID)).thenReturn(LOCATION);

        Optional<PublicationFiles> files = publicationFileGenerationService.generate(ARTEFACT_ID, sjpPublicListInput);
        verify(dataManagementService, never()).getArtefactJsonBlob(ARTEFACT_ID);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(files)
            .as(FILE_PRESENT_MESSAGE)
            .isPresent();

        softly.assertThat(files.get().getPrimaryPdf())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertThat(files.get().getAdditionalPdf())
            .as(FILE_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertThat(files.get().getExcel())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertAll();
    }

    @Test
    void testGenerateFilesSjpWelsh() {
        when(dataManagementService.getArtefact(ARTEFACT_ID)).thenReturn(WELSH_ARTEFACT);
        when(dataManagementService.getLocation(LOCATION_ID)).thenReturn(LOCATION);

        Optional<PublicationFiles> files = publicationFileGenerationService.generate(ARTEFACT_ID, sjpPublicListInput);
        verify(dataManagementService, never()).getArtefactJsonBlob(ARTEFACT_ID);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(files)
            .as(FILE_PRESENT_MESSAGE)
            .isPresent();

        softly.assertThat(files.get().getPrimaryPdf())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertThat(files.get().getAdditionalPdf())
            .as(FILE_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertThat(files.get().getExcel())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertAll();
    }

    @Test
    void testGenerateFilesNonSjpEnglish() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(dataManagementService.getArtefact(ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(dataManagementService.getLocation(LOCATION_ID)).thenReturn(LOCATION);

        Optional<PublicationFiles> files = publicationFileGenerationService.generate(ARTEFACT_ID, civilDailyListInput);
        verify(dataManagementService, never()).getArtefactJsonBlob(ARTEFACT_ID);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(files)
            .as(FILE_PRESENT_MESSAGE)
            .isPresent();

        softly.assertThat(files.get().getPrimaryPdf())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertThat(files.get().getAdditionalPdf())
            .as(FILE_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertThat(files.get().getExcel())
            .as(FILE_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertAll();
    }

    @Test
    void testGenerateFilesNonSjpWelsh() {
        WELSH_ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(dataManagementService.getArtefact(ARTEFACT_ID)).thenReturn(WELSH_ARTEFACT);
        when(dataManagementService.getLocation(LOCATION_ID)).thenReturn(LOCATION);

        Optional<PublicationFiles> files = publicationFileGenerationService.generate(ARTEFACT_ID, civilDailyListInput);
        verify(dataManagementService, never()).getArtefactJsonBlob(ARTEFACT_ID);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(files)
            .as(FILE_PRESENT_MESSAGE)
            .isPresent();

        softly.assertThat(files.get().getPrimaryPdf())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertThat(files.get().getAdditionalPdf())
            .as(FILE_NOT_EMPTY_MESSAGE)
            .isNotEmpty();

        softly.assertThat(files.get().getExcel())
            .as(FILE_EMPTY_MESSAGE)
            .isEmpty();

        softly.assertAll();
    }

    @Test
    void testGenerateFilesWithoutPayload() {
        ARTEFACT.setListType(ListType.CIVIL_DAILY_CAUSE_LIST);
        when(dataManagementService.getArtefactJsonBlob(ARTEFACT_ID)).thenReturn(civilDailyListInput);
        when(dataManagementService.getArtefact(ARTEFACT_ID)).thenReturn(ARTEFACT);
        when(dataManagementService.getLocation(LOCATION_ID)).thenReturn(LOCATION);

        publicationFileGenerationService.generate(ARTEFACT_ID, null);
        verify(dataManagementService).getArtefactJsonBlob(ARTEFACT_ID);
    }

    @Test
    void testGenerateFilesWithoutConverter() {
        ARTEFACT.setListType(ListType.SJP_PRESS_REGISTER);
        when(dataManagementService.getArtefact(any())).thenReturn(ARTEFACT);
        when(dataManagementService.getLocation(any())).thenReturn(LOCATION);

        assertThat(publicationFileGenerationService.generate(ARTEFACT_ID, sjpPublicListInput))
            .as(FILE_NOT_PRESENT_MESSAGE)
            .isEmpty();
    }

    @Test
    void testMaskDataSourceName() {
        ARTEFACT.setProvenance("SNL");
        assertThat(publicationFileGenerationService.maskDataSourceName(ARTEFACT.getProvenance()))
            .as("Provenance should be changed to ListAssist")
            .isEqualTo("ListAssist");
    }

    @Test
    void testDoNotMaskDataSourceName() {
        ARTEFACT.setProvenance("MANUAL_UPLOAD");
        assertThat(publicationFileGenerationService.maskDataSourceName(ARTEFACT.getProvenance()))
            .as("Provenance should not be changed")
            .isEqualTo("MANUAL_UPLOAD");
    }
}
