package uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.ArtefactSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.CivilDailyCauseListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.CopDailyCauseListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.DailyCauseListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.SjpPressListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.SjpPublicListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.SscsDailyListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.CivilAndFamilyDailyCauseListConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.CivilDailyCauseListConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.Converter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.CopDailyCauseListConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.FamilyDailyCauseListConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.SjpPressListConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.SjpPublicListConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.SscsDailyListConverter;

/**
 * Enum that represents the different list types.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ListType {
    SJP_PUBLIC_LIST(new SjpPublicListConverter(), new SjpPublicListSummaryConverter()),
    SJP_PRESS_LIST(new SjpPressListConverter(), new SjpPressListSummaryConverter()),
    SJP_PRESS_REGISTER,
    CROWN_DAILY_LIST,
    CROWN_FIRM_LIST,
    CROWN_WARNED_LIST,
    MAGISTRATES_PUBLIC_LIST,
    MAGISTRATES_STANDARD_LIST,
    IAC_DAILY_LIST,
    CIVIL_DAILY_CAUSE_LIST(new CivilDailyCauseListConverter(), new CivilDailyCauseListSummaryConverter()),
    FAMILY_DAILY_CAUSE_LIST(new FamilyDailyCauseListConverter(), new DailyCauseListSummaryConverter()),
    CIVIL_AND_FAMILY_DAILY_CAUSE_LIST(new CivilAndFamilyDailyCauseListConverter(),
                                      new DailyCauseListSummaryConverter()),
    COP_DAILY_CAUSE_LIST(new CopDailyCauseListConverter(), new CopDailyCauseListSummaryConverter()),
    SSCS_DAILY_LIST(new SscsDailyListConverter(), new SscsDailyListSummaryConverter()),
    PRIMARY_HEALTH_LIST,
    CARE_STANDARDS_LIST,
    ET_DAILY_LIST,
    ET_FORTNIGHTLY_PRESS_LIST;

    private Converter converter;
    private ArtefactSummaryConverter artefactSummaryConverter;
}
