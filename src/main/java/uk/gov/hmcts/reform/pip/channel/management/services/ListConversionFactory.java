package uk.gov.hmcts.reform.pip.channel.management.services;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.ArtefactSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.CivilDailyCauseListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.CopDailyCauseListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.CrownDailyListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.CrownFirmListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.CrownWarnedListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.EtDailyListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.EtFortnightlyPressListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.FamilyMixedDailyCauseListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.IacDailyListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.MagistratesPublicListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.MagistratesStandardListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.SjpPressListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.SjpPublicListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.SscsDailyListSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary.TribunalNationalListsSummaryConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.CareStandardsListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.CivilAndFamilyDailyCauseListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.CivilDailyCauseListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.CopDailyCauseListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.CrownDailyListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.CrownFirmListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.CrownWarnedListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.EtDailyListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.EtFortnightlyPressListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.FamilyDailyCauseListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.FileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.IacDailyListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.MagistratesPublicListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.MagistratesStandardListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.PrimaryHealthListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.SjpPressListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.SjpPublicListFileConverter;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.SscsDailyListFileConverter;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.Map;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.CARE_STANDARDS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CIVIL_AND_FAMILY_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CIVIL_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.COP_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CROWN_DAILY_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CROWN_FIRM_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CROWN_WARNED_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.ET_DAILY_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.ET_FORTNIGHTLY_PRESS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.FAMILY_DAILY_CAUSE_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.IAC_DAILY_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAGISTRATES_PUBLIC_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAGISTRATES_STANDARD_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.PRIMARY_HEALTH_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SJP_DELTA_PRESS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SJP_PRESS_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SJP_PUBLIC_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_DAILY_LIST;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.SSCS_DAILY_LIST_ADDITIONAL_HEARINGS;

@Component
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.UseConcurrentHashMap"})
public class ListConversionFactory {
    private static final Map<ListType, Pair<FileConverter, ArtefactSummaryConverter>> LIST_MAP = Map.ofEntries(
        Map.entry(SJP_PUBLIC_LIST, Pair.of(new SjpPublicListFileConverter(),
                                           new SjpPublicListSummaryConverter())),
        Map.entry(SJP_PRESS_LIST, Pair.of(new SjpPressListFileConverter(),
                                          new SjpPressListSummaryConverter())),
        Map.entry(SJP_DELTA_PRESS_LIST, Pair.of(new SjpPressListFileConverter(),
                                          new SjpPressListSummaryConverter())),
        Map.entry(CROWN_DAILY_LIST, Pair.of(new CrownDailyListFileConverter(),
                                            new CrownDailyListSummaryConverter())),
        Map.entry(CROWN_FIRM_LIST, Pair.of(new CrownFirmListFileConverter(),
                                           new CrownFirmListSummaryConverter())),
        Map.entry(CROWN_WARNED_LIST, Pair.of(new CrownWarnedListFileConverter(),
                                             new CrownWarnedListSummaryConverter())),
        Map.entry(MAGISTRATES_STANDARD_LIST, Pair.of(new MagistratesStandardListFileConverter(),
                                                     new MagistratesStandardListSummaryConverter())),
        Map.entry(MAGISTRATES_PUBLIC_LIST, Pair.of(new MagistratesPublicListFileConverter(),
                                                   new MagistratesPublicListSummaryConverter())),
        Map.entry(CIVIL_DAILY_CAUSE_LIST, Pair.of(new CivilDailyCauseListFileConverter(),
                                                  new CivilDailyCauseListSummaryConverter())),
        Map.entry(FAMILY_DAILY_CAUSE_LIST, Pair.of(new FamilyDailyCauseListFileConverter(),
                                                   new FamilyMixedDailyCauseListSummaryConverter())),
        Map.entry(CIVIL_AND_FAMILY_DAILY_CAUSE_LIST, Pair.of(new CivilAndFamilyDailyCauseListFileConverter(),
                                                             new FamilyMixedDailyCauseListSummaryConverter())),
        Map.entry(COP_DAILY_CAUSE_LIST, Pair.of(new CopDailyCauseListFileConverter(),
                                                new CopDailyCauseListSummaryConverter())),
        Map.entry(SSCS_DAILY_LIST, Pair.of(new SscsDailyListFileConverter(),
                                           new SscsDailyListSummaryConverter())),
        Map.entry(SSCS_DAILY_LIST_ADDITIONAL_HEARINGS, Pair.of(new SscsDailyListFileConverter(),
                                                               new SscsDailyListSummaryConverter())),
        Map.entry(IAC_DAILY_LIST, Pair.of(new IacDailyListFileConverter(),
                                          new IacDailyListSummaryConverter())),
        Map.entry(PRIMARY_HEALTH_LIST, Pair.of(new PrimaryHealthListFileConverter(),
                                               new TribunalNationalListsSummaryConverter())),
        Map.entry(CARE_STANDARDS_LIST, Pair.of(new CareStandardsListFileConverter(),
                                               new TribunalNationalListsSummaryConverter())),
        Map.entry(ET_DAILY_LIST, Pair.of(new EtDailyListFileConverter(),
                                         new EtDailyListSummaryConverter())),
        Map.entry(ET_FORTNIGHTLY_PRESS_LIST, Pair.of(new EtFortnightlyPressListFileConverter(),
                                                     new EtFortnightlyPressListSummaryConverter()))
    );

    public FileConverter getFileConverter(ListType listType) {
        if (LIST_MAP.containsKey(listType)) {
            return LIST_MAP.get(listType).getLeft();
        }
        return null;
    }

    public ArtefactSummaryConverter getArtefactSummaryConverter(ListType listType) {
        if (LIST_MAP.containsKey(listType)) {
            return LIST_MAP.get(listType).getRight();
        }
        return null;
    }
}
