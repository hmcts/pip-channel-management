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
                output.append("---\n");
                output.append("•Address - ").append(item.getDefendantInfo().getAddressWithoutPostcode());
                output.append("\n");
                output.append("Postcode - ").append(item.getDefendantInfo().getPostcode());
                output.append("\n");
                output.append("DOB - ").append(item.getDefendantInfo().getDob());
                output.append("\n");
                output.append("Case Ref / URN - ").append(item.getCaseInfo().getUrn());

                for (int i = 1; i <= item.getDefendantInfo().getOffences().size(); i++) {
                    Offence offence = item.getDefendantInfo().getOffences().get(i - 1);
                    output.append("\n");
                    output.append("Offence ").append(i).append(" Title - ").append(offence.getOffenceTitle());
                    output.append("\n");
                    output.append("Offence ").append(i).append(" Reporting Restriction - ")
                        .append(offence.getOffenceReportingRestriction());
                }

                output.append("\n");
                output.append("Reporting Restriction - ").append(item.getCaseInfo().getCaseReportingRestriction());
                output.append("\n");
                output.append("Prosecutor - ").append(item.getDefendantInfo().getProsecutor());
                output.append("\n");
            });
        });

        return output.toString();
    }

}
