package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.CaseHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.CommonListHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.MagistratesPublicListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

@Service
public class MagistratesPublicListSummaryConverter implements ArtefactSummaryConverter {
    private static final String LISTING_NOTES = "listingNotes";

    private static void appendAdditionalListInfo(StringBuilder output, JsonNode sitting, JsonNode hearing,
                                                     JsonNode hearingCase) {
        GeneralHelper.appendToStringBuilder(output, "Prosecuting Authority - ", hearing, "prosecutingAuthority");
        output.append('\n');

        String formattedDuration = "Duration - " + CaseHelper.appendCaseSequenceIndicator(
            GeneralHelper.findAndReturnNodeText(sitting, "formattedDuration"),
            GeneralHelper.findAndReturnNodeText(hearingCase, "caseSequenceIndicator")
        );
        output.append(formattedDuration);

        if (!GeneralHelper.findAndReturnNodeText(hearing, LISTING_NOTES).isEmpty()) {
            GeneralHelper.appendToStringBuilder(output, "Case Details - ", hearing, LISTING_NOTES);
        }
    }

    /**
     * Magistrates public lists - iterates on courtHouse/courtList.
     *
     * @param payload - json body.
     * @return - string for output.
     * @throws JsonProcessingException - jackson req.
     */
    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        CommonListHelper.manipulatedListData(payload, Language.ENGLISH, false);
        MagistratesPublicListHelper.manipulatedMagistratesPublicListData(payload);

        return processMagistratesPublicList(payload);
    }

    public String processMagistratesPublicList(JsonNode node) {
        StringBuilder output = new StringBuilder();
        node.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(
                            hearing -> hearing.get("case").forEach(hearingCase -> {
                                output.append('\n');
                                GeneralHelper.appendToStringBuilder(output, "Sitting at - ",
                                                                    sitting, "time");
                                GeneralHelper.appendToStringBuilder(output, "Case Reference - ",
                                                                    hearingCase, "caseNumber");
                                GeneralHelper.appendToStringBuilder(output, "Defendant Name(s) - ",
                                                                    hearing, "defendant");
                                GeneralHelper.appendToStringBuilder(output, "Hearing Type - ",
                                                                    hearing, "hearingType");

                                appendAdditionalListInfo(output, sitting, hearing, hearingCase);
                            })
                        )
                    )
                )
            )
        );
        return output.toString();
    }
}
