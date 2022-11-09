package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation.MagistratesStandardListHelper;

import java.util.Map;

@Service
public class MagistratesStandardListSummaryConverter implements ArtefactSummaryConverter  {
    /**
     * Magistrates Standard List parent method - iterates on courtHouse/courtList - if these need to be shown in further
     * iterations, do it here.
     *
     * @param payload - json body.
     * @return - string for output.
     * @throws JsonProcessingException - jackson req.
     */
    @Override
    public String convert(String payload) throws JsonProcessingException {
        JsonNode node = new ObjectMapper().readTree(payload);
        Map<String, Object> language =
            Map.of("age", "Age: ");
        DataManipulation.manipulatedDailyListData(node, Language.ENGLISH, false);
        MagistratesStandardListHelper.manipulatedMagistratesStandardList(node, language);
        return this.processMagistratesStandardList(node);
    }

    private String processMagistratesStandardList(JsonNode node) {
        StringBuilder output = new StringBuilder();
        node.get("courtLists").forEach(courtList -> {
            courtList.get("courtHouse").get("courtRoom").forEach(courtRoom -> {
                courtRoom.get("session").forEach(session -> {
                    session.get("sittings").forEach(sitting -> {
                        sitting.get("hearing").forEach(hearing -> {
                            hearing.get("case").forEach(hearingCase -> {
                                output.append('\n');
                                formatDefendantHeading(output, hearing);
                                output.append('\n');
                                formatSittingTime(output, sitting, hearingCase);

                                checkAndAddToEmail(output, hearing, "defendantDateOfBirthAndAge",
                                                   "DOB and Age - ");
                                checkAndAddToEmail(output, hearing, "defendantAddress",
                                                   "Defendant Address - ");
                                checkAndAddToEmail(output, hearingCase.get("informant"), "prosecutionAuthorityCode",
                                                   "Prosecuting Authority - ");
                                checkAndAddToEmail(output, hearingCase, "hearingNumber",
                                                   "Hearing Number - ");
                                checkAndAddToEmail(output, sitting, "caseHearingChannel",
                                                   "Attendance Method - ");
                                checkAndAddToEmail(output, hearingCase, "caseNumber",
                                                   "Case Ref - ");
                                output.append('\n');
                                formatAsn(output);
                                checkAndAddToEmail(output, hearing, "hearingType",
                                                   "Hearing of Type - ");
                                checkAndAddToEmail(output, hearingCase, "panel",
                                                   "Panel - ");
                                output.append('\n');

                                hearing.get("offence").forEach(offence -> {
                                    checkAndAddToEmail(output, offence, "offenceTitle","");
                                    checkAndAddToEmail(output, hearing, "plea","Plea - ");
                                    output.append('\n');
                                    formatDateOfPlea(output);
                                    checkAndAddToEmail(output, hearingCase, "formattedConvictionDate",
                                                       "Convicted on - ");
                                    output.append('\n');
                                    formatAdjournedFrom(output, hearingCase);
                                    checkAndAddToEmail(output, offence, "offenceWording",
                                                       "");
                                    output.append('\n');
                                });
                            });
                        });
                    });
                });
            });
        });

        return output.toString();
    }

    private void formatDefendantHeading(StringBuilder output, JsonNode hearing) {
        String defendantInfo = hearing.get("defendantHeading").asText()
            + " " + hearing.get("gender").asText()
            + " " + hearing.get("inCustody").asText();
        output.append("Defendant Name - ").append(defendantInfo).append('\n');
    }

    private void formatSittingTime(StringBuilder output, JsonNode sitting, JsonNode hearingCase) {
        String sittingTime = sitting.get("time").asText() + " " + "for "
            + sitting.get("formattedDuration").asText()
            + " " + hearingCase.get("caseSequenceIndicator").asText();
        output.append("Sitting at - ").append(sittingTime);
    }

    private void formatAsn(StringBuilder output) {
        output.append("ASN - Need to confirm");
    }

    private void formatDateOfPlea(StringBuilder output) {
        output.append("Date of Plea - Need to confirm");
    }

    private void formatAdjournedFrom(StringBuilder output, JsonNode hearingCase) {
        if (!GeneralHelper.findAndReturnNodeText(hearingCase,
            "formattedAdjournedDate").isBlank()) {
            String adjournedFrom = hearingCase.get("formattedAdjournedDate").asText() + " - "
                + "For the trial";
            output.append("Adjourned from - ").append(adjournedFrom);
        }
    }

    private void checkAndAddToEmail(StringBuilder output, JsonNode node, String nodeText, String text) {
        if (!GeneralHelper.findAndReturnNodeText(node, nodeText).isBlank()) {
            GeneralHelper.appendToStringBuilder(output, text, node, nodeText);
        }
    }
}
