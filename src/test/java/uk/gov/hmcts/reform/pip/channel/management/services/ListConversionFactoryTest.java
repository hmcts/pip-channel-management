package uk.gov.hmcts.reform.pip.channel.management.services;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.SjpPressListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.CivilDailyCauseListFileConverter;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(profiles = "test")
class ListConversionFactoryTest {
    private final ListConversionFactory listConversionFactory = new ListConversionFactory();

    @Test
    void testGetFileConverter() {
        assertThat(listConversionFactory.getFileConverter(ListType.CIVIL_DAILY_CAUSE_LIST))
            .as("File converter does not match")
            .isInstanceOf(CivilDailyCauseListFileConverter.class);
    }

    @Test
    void testGetFileConverterNotFound() {
        assertThat(listConversionFactory.getFileConverter(ListType.SJP_PRESS_REGISTER))
            .as("File converter is not null")
            .isNull();
    }

    @Test
    void testGetArtefactSummeryConverter() {
        assertThat(listConversionFactory.getArtefactSummaryConverter(ListType.SJP_PRESS_LIST))
            .as("Artefact summary does not match")
            .isInstanceOf(SjpPressListSummaryConverter.class);
    }

    @Test
    void testGetArtefactSummeryConverterNotFound() {
        assertThat(listConversionFactory.getArtefactSummaryConverter(ListType.SJP_PRESS_REGISTER))
            .as("Artefact summary is not null")
            .isNull();
    }

}
