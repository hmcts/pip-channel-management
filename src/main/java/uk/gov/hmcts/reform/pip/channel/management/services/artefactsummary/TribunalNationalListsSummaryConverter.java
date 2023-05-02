package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.TribunalNationalList;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.TribunalNationalListsManipulation;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.List;

@Service
public class TribunalNationalListsSummaryConverter implements ArtefactSummaryConverter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convert(String payload) throws JsonProcessingException {
        StringBuilder output = new StringBuilder(80);
        JsonNode jsonPayload = OBJECT_MAPPER.readTree(payload);

        LocationHelper.formatCourtAddress(jsonPayload, ", ", true);
        List<TribunalNationalList> tribunalNationalList =
            TribunalNationalListsManipulation.processRawListData(jsonPayload, Language.ENGLISH);

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
