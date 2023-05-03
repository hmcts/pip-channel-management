package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.SjpPublicListHelper;

import java.util.Optional;

@Service
public class SjpPublicListSummaryConverter implements ArtefactSummaryConverter {
    private static final String HEARING = "hearing";
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";

    /**
     * parent method for sjp public lists. iterates on sittings.
     *
     * @param payload - json body.
     * @return string of data.
     * @throws JsonProcessingException - jackson prereq.
     */
    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        StringBuilder output = new StringBuilder();

        payload.get(COURT_LISTS).forEach(courtList ->
            courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(
                    session -> session.get(SITTINGS).forEach(
                        sitting -> sitting.get(HEARING).forEach(hearing -> {
                            Optional<uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.SjpPublicList>
                                sjpCaseOptional = SjpPublicListHelper.constructSjpCase(hearing);
                            if (sjpCaseOptional.isPresent()) {
                                uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.SjpPublicList
                                    sjpCase = sjpCaseOptional.get();
                                output
                                    .append("•Defendant: ")
                                    .append(sjpCase.getName())
                                    .append("\nPostcode: ")
                                    .append(sjpCase.getPostcode())
                                    .append("\nProsecutor: ")
                                    .append(sjpCase.getProsecutor())
                                    .append("\nOffence: ")
                                    .append(sjpCase.getOffence())
                                    .append('\n');
                            }
                        })
                    )
                )
            )
        );
        return output.toString();
    }
}
