package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.CrownWarnedList;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.CrownWarnedListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class CrownWarnedListSummaryConverter implements ArtefactSummaryConverter {
    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        StringBuilder output = new StringBuilder(140);

        Map<String, List<CrownWarnedList>> cases = GeneralHelper.hearingHasParty(payload)
            ? CrownWarnedListHelper.processRawListDataV1(payload, Language.ENGLISH)
            : CrownWarnedListHelper.processRawListData(payload, Language.ENGLISH);

        cases.values()
            .stream()
            .flatMap(Collection::stream)
            .forEach(
                row -> output
                    .append("\t•Case Reference: ")
                    .append(row.getCaseReference())
                    .append("\n\t\tDefendant Name(s): ")
                    .append(row.getDefendant())
                    .append("\nFixed For: ")
                    .append(row.getHearingDate())
                    .append("\nRepresented By: ")
                    .append(row.getDefendantRepresentative())
                    .append("\nProsecuting Authority: ")
                    .append(row.getProsecutingAuthority())
                    .append("\nLinked Cases: ")
                    .append(row.getLinkedCases())
                    .append("\nListing Notes: ")
                    .append(row.getListingNotes())
                    .append(System.lineSeparator())
            );

        return output.toString();
    }
}
