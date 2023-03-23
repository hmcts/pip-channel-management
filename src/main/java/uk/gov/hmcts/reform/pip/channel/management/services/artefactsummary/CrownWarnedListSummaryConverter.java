package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation.CrownWarnedListManipulation;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.Collection;

@Service
public class CrownWarnedListSummaryConverter implements ArtefactSummaryConverter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convert(String payload) throws JsonProcessingException {
        JsonNode jsonPayload = OBJECT_MAPPER.readTree(payload);
        StringBuilder output = new StringBuilder(140);
        CrownWarnedListManipulation.processRawListData(jsonPayload, Language.ENGLISH)
            .values()
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
