package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;

@Service
public class DailyCauseListSummaryConverter implements ArtefactSummaryConverter {

    /**
     * Civil cause list parent method - iterates on courtHouse/courtList - if these need to be shown in further
     * iterations, do it here.
     *
     * @param payload - json body.
     * @return - string for output.
     * @throws JsonProcessingException - jackson req.
     */
    @Override
    public String convert(String payload) throws JsonProcessingException {
        JsonNode node = new ObjectMapper().readTree(payload);

        DataManipulation.manipulatedDailyListData(node, Language.ENGLISH, false);

        return this.processDailyCauseList(node);
    }

    public String processDailyCauseList(JsonNode node) {
        StringBuilder output = new StringBuilder();
        node.get("courtLists").forEach(courtList -> {
            courtList.get("courtHouse").get("courtRoom").forEach(courtRoom -> {
                courtRoom.get("session").forEach(session -> {
                    session.get("sittings").forEach(sitting -> {
                        sitting.get("hearing").forEach(hearing -> {
                            hearing.get("case").forEach(hearingCase -> {
                                output.append('\n');
                                GeneralHelper.appendToStringBuilder(output, "Name of Party(ies) - ",
                                                                    hearingCase, "caseName");
                                GeneralHelper.appendToStringBuilder(output, "Case ID - ",
                                                                    hearingCase, "caseNumber");
                                output.append('\n');
                                GeneralHelper.appendToStringBuilder(output, "Hearing Type - ",
                                                                    hearing, "hearingType");
                                GeneralHelper.appendToStringBuilder(output, "Location - ",
                                                                    sitting, "caseHearingChannel");
                                GeneralHelper.appendToStringBuilder(output, "Duration - ",
                                                                    sitting, "formattedDuration");
                                GeneralHelper.appendToStringBuilder(output, "Judge - ",
                                                                    session,"formattedSessionCourtRoom");
                            });
                        });
                    });
                });
            });
        });

        return output.toString();
    }
}
