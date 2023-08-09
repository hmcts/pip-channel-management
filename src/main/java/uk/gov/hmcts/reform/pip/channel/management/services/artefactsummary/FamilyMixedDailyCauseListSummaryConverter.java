package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.FamilyMixedListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

@Service
public class FamilyMixedDailyCauseListSummaryConverter implements ArtefactSummaryConverter {
    /**
     * Family and mixed daily cause list method that generates the summary in the email.
     *
     * @param payload - json body.
     * @return - string for output.
     * @throws JsonProcessingException - jackson req.
     */
    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        FamilyMixedListHelper.manipulatedlistData(payload, Language.ENGLISH);
        return this.processDailyCauseList(payload);
    }

    public String processDailyCauseList(JsonNode node) {
        StringBuilder output = new StringBuilder();
        node.get("courtLists")
            .forEach(courtList -> courtList.get("courtHouse").get("courtRoom")
                .forEach(courtRoom -> courtRoom.get("session")
                    .forEach(session -> session.get("sittings")
                        .forEach(sitting -> sitting.get("hearing")
                            .forEach(hearing -> hearing.get("case")
                                .forEach(hearingCase -> {
                                    output.append('\n');
                                    GeneralHelper.appendToStringBuilder(output, "Case Name - ",
                                                                        hearingCase, "caseName"
                                    );
                                    GeneralHelper.appendToStringBuilder(output, "Case ID - ",
                                                                        hearingCase, "caseNumber"
                                    );
                                    output.append('\n');
                                    GeneralHelper.appendToStringBuilder(output, "Hearing Type - ",
                                                                        hearing, "hearingType"
                                    );
                                    GeneralHelper.appendToStringBuilder(output, "Location - ",
                                                                        sitting, "caseHearingChannel"
                                    );
                                    GeneralHelper.appendToStringBuilder(output, "Duration - ",
                                                                        sitting, "formattedDuration"
                                    );
                                    GeneralHelper.appendToStringBuilder(output, "Judge - ",
                                                                        session, "formattedSessionCourtRoom"
                                    );
                                }))))));

        return output.toString();
    }
}
