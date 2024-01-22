package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.oparesults.Offence;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.OpaResultsHelper;

public class OpaResultsSummaryConverter implements ArtefactSummaryConverter {
    private static final String NEW_LINE = "\n";
    private static final String OFFENCE = "Offence ";

    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        StringBuilder output = new StringBuilder(300);

        OpaResultsHelper.processRawListData(payload).forEach((pleaData, list) ->
            list.forEach(item -> {
                output
                    .append("---\n•Defendant Name - ").append(item.getDefendant())
                    .append("\nCase Ref / URN - ").append(item.getCaseUrn());

                for (int i = 1; i <= item.getOffences().size(); i++) {
                    Offence offence = item.getOffences().get(i - 1);
                    output
                        .append(NEW_LINE)
                        .append(OFFENCE).append(i).append(" Title - ")
                        .append(offence.getOffenceTitle())
                        .append(NEW_LINE)
                        .append(OFFENCE).append(i).append(" Section - ")
                        .append(offence.getOffenceSection())
                        .append(NEW_LINE)
                        .append(OFFENCE).append(i).append(" Decision Date - ")
                        .append(offence.getDecisionDate())
                        .append(NEW_LINE)
                        .append(OFFENCE).append(i).append(" Allocation Decision - ")
                        .append(offence.getDecisionDetail())
                        .append(NEW_LINE)
                        .append(OFFENCE).append(i).append(" Bail Status - ")
                        .append(offence.getBailStatus())
                        .append(NEW_LINE)
                        .append(OFFENCE).append(i).append(" Next Hearing Date - ")
                        .append(offence.getNextHearingDate())
                        .append(NEW_LINE)
                        .append(OFFENCE).append(i).append(" Next Hearing Location - ")
                        .append(offence.getNextHearingLocation())
                        .append(NEW_LINE)
                        .append(OFFENCE).append(i).append(" Reporting Restrictions - ")
                        .append(offence.getReportingRestrictions());
                }
                output.append(NEW_LINE);
            })
        );

        return output.toString();
    }
}
