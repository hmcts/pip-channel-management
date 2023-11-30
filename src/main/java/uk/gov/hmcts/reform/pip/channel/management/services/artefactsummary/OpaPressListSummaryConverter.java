package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.opapresslist.Offence;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.OpaPressListHelper;

@Service
public class OpaPressListSummaryConverter implements ArtefactSummaryConverter {

    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        StringBuilder output = new StringBuilder();

        OpaPressListHelper.processRawListData(payload).forEach((pleaData, list) -> {
            list.forEach(item -> {
                output
                    .append("---\n")
                    .append("•Address - ").append(item.getDefendantInfo().getAddressWithoutPostcode())
                    .append("\n")
                    .append("Postcode - ").append(item.getDefendantInfo().getPostcode())
                    .append("\n")
                    .append("DOB - ").append(item.getDefendantInfo().getDob())
                    .append("\n")
                    .append("Case Ref / URN - ").append(item.getCaseInfo().getUrn());

                for (int i = 1; i <= item.getDefendantInfo().getOffences().size(); i++) {
                    Offence offence = item.getDefendantInfo().getOffences().get(i - 1);
                    output.append("\n")
                        .append("Offence ").append(i).append(" Title - ").append(offence.getOffenceTitle())
                        .append("\n")
                        .append("Offence ").append(i).append(" Reporting Restriction - ")
                        .append(offence.getOffenceReportingRestriction());
                }

                output.append("\n")
                    .append("Reporting Restriction - ").append(item.getCaseInfo().getCaseReportingRestriction())
                    .append("\n")
                    .append("Prosecutor - ").append(item.getDefendantInfo().getProsecutor())
                    .append("\n");
            });
        });

        return output.toString();
    }

}
