package uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.ArtefactSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.CivilDailyCauseListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.CopDailyCauseListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.CrownDailyListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.CrownWarnedListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.DailyCauseListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.EtDailyListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.EtFortnightlyPressListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.IacDailyListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.MagistratesPublicListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.MagistratesStandardListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.SjpPressListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.SjpPublicListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.SscsDailyListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.TribunalNationalListsSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.CareStandardsListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.CivilAndFamilyDailyCauseListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.CivilDailyCauseListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.CopDailyCauseListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.CrownDailyListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.CrownWarnedListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.EtDailyListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.EtFortnightlyPressListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.FamilyDailyCauseListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.FileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.IacDailyListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.MagistratesPublicListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.MagistratesStandardListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.PrimaryHealthListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.SjpPressListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.SjpPublicListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters.SscsDailyListFileConverter;

/**
 * Enum that represents the different list types.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("PMD")
public enum ListType {
    SJP_PUBLIC_LIST(new SjpPublicListFileConverter(), new SjpPublicListSummaryConverter()),
    SJP_PRESS_LIST(new SjpPressListFileConverter(), new SjpPressListSummaryConverter()),
    SJP_PRESS_REGISTER,
    CROWN_DAILY_LIST(new CrownDailyListFileConverter(), new CrownDailyListSummaryConverter()),
    CROWN_FIRM_LIST,
    MAGISTRATES_STANDARD_LIST(new MagistratesStandardListFileConverter(),
                              new MagistratesStandardListSummaryConverter()),
    MAGISTRATES_PUBLIC_LIST(new MagistratesPublicListFileConverter(),
                            new MagistratesPublicListSummaryConverter()),
    CROWN_WARNED_LIST(new CrownWarnedListFileConverter(), new CrownWarnedListSummaryConverter()),
    IAC_DAILY_LIST(new IacDailyListFileConverter(), new IacDailyListSummaryConverter()),
    CIVIL_DAILY_CAUSE_LIST(new CivilDailyCauseListFileConverter(), new CivilDailyCauseListSummaryConverter()),
    FAMILY_DAILY_CAUSE_LIST(new FamilyDailyCauseListFileConverter(), new DailyCauseListSummaryConverter()),
    CIVIL_AND_FAMILY_DAILY_CAUSE_LIST(new CivilAndFamilyDailyCauseListFileConverter(),
                                      new DailyCauseListSummaryConverter()),
    COP_DAILY_CAUSE_LIST(new CopDailyCauseListFileConverter(), new CopDailyCauseListSummaryConverter()),
    SSCS_DAILY_LIST(new SscsDailyListFileConverter(), new SscsDailyListSummaryConverter()),
    PRIMARY_HEALTH_LIST(new PrimaryHealthListFileConverter(), new TribunalNationalListsSummaryConverter()),
    CARE_STANDARDS_LIST(new CareStandardsListFileConverter(), new TribunalNationalListsSummaryConverter()),
    ET_DAILY_LIST(new EtDailyListFileConverter(), new EtDailyListSummaryConverter()),
    ET_FORTNIGHTLY_PRESS_LIST(new EtFortnightlyPressListFileConverter(), new EtFortnightlyPressListSummaryConverter());

    private FileConverter fileConverter;
    private ArtefactSummaryConverter artefactSummaryConverter;
}
