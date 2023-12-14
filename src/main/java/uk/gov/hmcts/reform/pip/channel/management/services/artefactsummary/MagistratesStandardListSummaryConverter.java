package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.magistratesstandardlist.CaseInfo;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.magistratesstandardlist.CaseSitting;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.magistratesstandardlist.DefendantInfo;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.magistratesstandardlist.Offence;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.MagistratesStandardListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

@Service
public class MagistratesStandardListSummaryConverter implements ArtefactSummaryConverter {
    /**
     * Summary class for the Magistrates standard list that generates the summary in the email.
     *
     * @param payload - json body.
     * @return - string for output.
     * @throws JsonProcessingException - jackson req.
     */
    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        StringBuilder output = new StringBuilder(256);

        MagistratesStandardListHelper.processRawListData(payload, Language.ENGLISH)
            .forEach(
                (courtRoom, list) -> list.forEach(item -> {
                    output
                        .append("\t•Defendant Name - ")
                        .append(item.getDefendantHeading());

                    item.getCaseSittings().forEach(sitting -> {
                        CaseInfo caseInfo = sitting.getCaseInfo();
                        output
                            .append("\n\t\tSitting at - ")
                            .append(formatSittingHeading(sitting))
                            .append("\nDOB and Age - ")
                            .append(formatDefendantDobAndAge(sitting.getDefendantInfo()))
                            .append("\nDefendant Address - ")
                            .append(sitting.getDefendantInfo().getAddress())
                            .append("\nProsecuting Authority - ")
                            .append(caseInfo.getProsecutingAuthorityCode())
                            .append("\nHearing Number - ")
                            .append(caseInfo.getHearingNumber())
                            .append("\nAttendance Method - ")
                            .append(caseInfo.getAttendanceMethod())
                            .append("\nCase Ref - ")
                            .append(caseInfo.getCaseNumber())
                            .append("\nASN - ")
                            .append(caseInfo.getAsn())
                            .append("\nHearing of Type - ")
                            .append(caseInfo.getHearingType())
                            .append("\nPanel - ")
                            .append(caseInfo.getPanel());

                        appendOffences(output, sitting);
                    });
                    output.append("\n\n");
                })
            );
        return output.toString();
    }

    private String formatSittingHeading(CaseSitting sitting) {
        String sittingDuration = sitting.getSittingDuration();
        String caseSequenceIndicator = sitting.getCaseInfo().getCaseSequenceIndicator();

        return sitting.getSittingStartTime()
            + (sittingDuration.isEmpty() ? "" : " for " + sittingDuration)
            + (caseSequenceIndicator.isEmpty() ? "" : " [" + caseSequenceIndicator + "]");
    }

    private String formatDefendantDobAndAge(DefendantInfo defendantInfo) {
        return defendantInfo.getDob() + (defendantInfo.getAge().isEmpty()
            ? "" : " Age: " + defendantInfo.getAge());
    }

    private void appendOffences(StringBuilder output, CaseSitting sitting) {
        int offenceIndex = 1;
        for (Offence offence : sitting.getOffences()) {
            output
                .append(formatOffenceType(offenceIndex, "Title"))
                .append(offence.getOffenceTitle())
                .append(formatOffenceType(offenceIndex, "Plea"))
                .append(sitting.getDefendantInfo().getPlea())
                .append(formatOffenceType(offenceIndex, "Date of Plea"))
                .append(sitting.getDefendantInfo().getPleaDate())
                .append(formatOffenceType(offenceIndex, "Convicted on"))
                .append(sitting.getCaseInfo().getConvictionDate())
                .append(formatOffenceType(offenceIndex, "Adjourned from"))
                .append(sitting.getCaseInfo().getAdjournedDate())
                .append(" - For the trial")
                .append(formatOffenceType(offenceIndex, "Details"))
                .append(offence.getOffenceWording());
            offenceIndex++;
        }
    }

    private String formatOffenceType(int offenceIndex, String type) {
        return "\nOffence " + offenceIndex + " " + type + " - ";
    }
}
