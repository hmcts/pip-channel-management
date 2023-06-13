package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.CaseHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.SittingHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

/**
 * Summary class for the IAC Daily List that generates the summary in the email.
 */
@Service
public class IacDailyListSummaryConverter implements ArtefactSummaryConverter {
    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        StringBuilder output = new StringBuilder(30);
        payload.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(sitting -> {
                        String sittingStart = DateHelper.formatTimeStampToBst(
                            sitting.get("sittingStart").asText(), Language.ENGLISH, false, false, "h:mma"
                        );
                        SittingHelper.findAndConcatenateHearingPlatform(sitting, session);
                        sitting.get("hearing").forEach(hearing -> {
                            PartyRoleHelper.findAndManipulatePartyInformation(hearing, false);
                            hearing.get("case").forEach(hearingCase -> {
                                GeneralHelper.appendToStringBuilder(output, "List Name - ",
                                                                    courtList, "courtListName");
                                output
                                    .append("\nStart Time - ")
                                    .append(sittingStart)
                                    .append("\nCase Ref - ")
                                    .append(CaseHelper.appendCaseSequenceIndicator(
                                        GeneralHelper.findAndReturnNodeText(hearingCase, "caseNumber"),
                                        GeneralHelper.findAndReturnNodeText(hearingCase, "caseSequenceIndicator")
                                    ));

                                GeneralHelper.appendToStringBuilder(output, "Hearing Channel - ",
                                                                    sitting, "caseHearingChannel");
                                GeneralHelper.appendToStringBuilder(output, "Appellant - ",
                                                                    hearing, "claimant");
                                GeneralHelper.appendToStringBuilder(output, "Prosecuting Authority - ",
                                                                    hearing, "prosecutingAuthority");
                                output.append('\n');
                            });
                        });
                    })
                )
            )
        );

        return output.toString();
    }
}
