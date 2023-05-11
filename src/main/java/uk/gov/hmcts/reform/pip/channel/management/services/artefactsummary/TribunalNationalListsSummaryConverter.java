package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.TribunalNationalList;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.TribunalNationalListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.List;

@Service
public class TribunalNationalListsSummaryConverter implements ArtefactSummaryConverter {
    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        StringBuilder output = new StringBuilder(80);

        LocationHelper.formatCourtAddress(payload, ", ", true);
        List<TribunalNationalList> tribunalNationalList =
            TribunalNationalListHelper.processRawListData(payload, Language.ENGLISH);

        tribunalNationalList.forEach(
            data -> output
                .append("•Hearing Date: ")
                .append(data.getHearingDate())
                .append("\n\tCase Name: ")
                .append(data.getCaseName())
                .append("\nDuration: ")
                .append(data.getDuration())
                .append("\nHearing Type: ")
                .append(data.getHearingType())
                .append("\nVenue: ")
                .append(data.getVenue())
                .append('\n')
        );
        return output.toString();
    }
}
