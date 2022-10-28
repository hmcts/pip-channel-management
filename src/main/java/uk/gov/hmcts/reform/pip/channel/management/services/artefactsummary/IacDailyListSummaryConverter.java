package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;

/**
 * Summary class for the IAC Daily List that generates the summary in the email.
 */
@Service
public class IacDailyListSummaryConverter implements ArtefactSummaryConverter {

    @Override
    public String convert(String payload) throws JsonProcessingException {
        JsonNode node = new ObjectMapper().readTree(payload);

        StringBuilder output = new StringBuilder();
        node.get("courtLists").forEach(courtList -> {
            courtList.get("courtHouse").get("courtRoom").forEach(courtRoom -> {
                courtRoom.get("session").forEach(session -> {
                    session.get("sittings").forEach(sitting -> {

                        String sittingStart = DateHelper.timeStampToBstTimeWithFormat(
                            sitting.get("sittingStart").asText(), "h:mma");

                        DataManipulation.findAndConcatenateHearingPlatform(sitting, session);
                        sitting.get("hearing").forEach(hearing -> {
                            DataManipulation.findAndManipulatePartyInformation(hearing, Language.ENGLISH);
                            hearing.get("case").forEach(hearingCase -> {
                                GeneralHelper.appendToStringBuilder(output, "List Name - ",
                                                                    courtList, "courtListName");
                                output.append("\nStart Time - ");
                                output.append(sittingStart);
                                GeneralHelper.appendToStringBuilder(output, "Case Ref - ",
                                                                    hearingCase, "caseNumber");
                                GeneralHelper.appendToStringBuilder(output, "Hearing Channel - ",
                                                                    sitting, "caseHearingChannel");
                                GeneralHelper.appendToStringBuilder(output, "Appellant - ",
                                                                    hearing, "claimant");
                                GeneralHelper.appendToStringBuilder(output, "Prosecuting Authority - ",
                                                                    hearing, "prosecutingAuthority");
                                output.append('\n');
                            });
                        });
                    });
                });
            });
        });

        return output.toString();
    }
}