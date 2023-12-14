package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.opapubliclist.Offence;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.OpaPublicListHelper;

@Service
public class OpaPublicListSummaryConverter implements ArtefactSummaryConverter {

    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        StringBuilder output = new StringBuilder(256);

        OpaPublicListHelper.formatOpaPublicList(payload).forEach(list -> {
                output
                        .append("---\n•Defendant - ").append(list.getDefendant().getName())
                        .append("\nCase Ref / URN - ").append(list.getCaseInfo().getUrn());

                for (int i = 1; i <= list.getDefendant().getOffences().size(); i++) {
                    Offence offence = list.getDefendant().getOffences().get(i - 1);
                    output
                            .append("\nOffence ").append(i).append(" Title - ").append(offence.getOffenceTitle())
                            .append("\nOffence ").append(i).append(" Reporting Restriction - ")
                            .append(offence.getOffenceReportingRestriction());
                }

                output
                        .append("\nReporting Restriction - ").append(list.getCaseInfo().getCaseReportingRestriction())
                        .append("\nProsecutor - ").append(list.getDefendant().getProsecutor())
                        .append('\n');
        });

        return output.toString();
    }

}
