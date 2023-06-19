package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.CaseHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.CopListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

@Service
public class CopDailyCauseListSummaryConverter implements ArtefactSummaryConverter {
    /**
     * COP Daily Cause List summary producer.
     *
     * @param payload - The artefact.
     * @return - The returned summary for the list.
     * @throws JsonProcessingException - Thrown if there has been an error while processing the JSON payload.
     */
    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        CopListHelper.manipulateCopListData(payload, Language.ENGLISH);
        return this.processCopDailyCauseList(payload);
    }

    /**
     * Loops through the artefact and creates the summary.
     *
     * @param node - The artefact to process
     * @return String containing the summary for the list.
     */
    private String processCopDailyCauseList(JsonNode node) {
        StringBuilder output = new StringBuilder(100);
        node.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(
                            hearing -> hearing.get("case").forEach(
                                hearingCase -> {
                                    output
                                        .append("\n\nName of Party(ies) - ")
                                        .append(GeneralHelper.findAndReturnNodeText(hearingCase, "caseSuppressionName"))
                                        .append("\nCase ID - ")
                                        .append(GeneralHelper.findAndReturnNodeText(hearingCase, "caseNumber"))
                                        .append("\nHearing Type - ")
                                        .append(GeneralHelper.findAndReturnNodeText(hearing, "hearingType"))
                                        .append("\nLocation - ")
                                        .append(GeneralHelper.findAndReturnNodeText(sitting, "caseHearingChannel"))
                                        .append("\nDuration - ")
                                        .append(CaseHelper.appendCaseSequenceIndicator(
                                            GeneralHelper.findAndReturnNodeText(sitting, "formattedDuration"),
                                            GeneralHelper.findAndReturnNodeText(hearingCase, "caseIndicator")
                                        ))
                                        .append("\nBefore - ")
                                        .append(GeneralHelper.findAndReturnNodeText(session, "formattedSessionJoh"));
                                }
                            )
                        )
                    )
                )
            )
        );

        return output.toString();
    }
}
