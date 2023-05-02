package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.CrimeListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

@Service
public class MagistratesPublicListSummaryConverter implements ArtefactSummaryConverter {
    private static final String LISTING_NOTES = "listingNotes";

    private static void appendAdditionalListInfo(StringBuilder output, JsonNode sitting, JsonNode hearing,
                                                     JsonNode hearingCase) {
        GeneralHelper.appendToStringBuilder(output, "Prosecuting Authority - ", hearing, "prosecutingAuthority");
        output.append('\n');

        String caseSequenceNo = " " + GeneralHelper.findAndReturnNodeText(hearingCase, "caseSequenceIndicator");

        String formattedDuration = "Duration - "
            + GeneralHelper.findAndReturnNodeText(sitting, "formattedDuration")
            + caseSequenceNo;
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
    public String convert(String payload) throws JsonProcessingException {
        JsonNode node = new ObjectMapper().readTree(payload);

        DataManipulation.manipulatedDailyListData(node, Language.ENGLISH, false);
        CrimeListHelper.manipulatedCrimeListData(node, ListType.MAGISTRATES_PUBLIC_LIST);

        return processMagistratesPublicList(node);
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
