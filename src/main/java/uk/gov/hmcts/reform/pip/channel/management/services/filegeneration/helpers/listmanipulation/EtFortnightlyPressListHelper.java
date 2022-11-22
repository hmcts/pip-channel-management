package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.LocationHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.SittingHelper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DailyCauseListHelper.preprocessArtefactForThymeLeafConverter;

@Slf4j
@SuppressWarnings("PMD.TooManyMethods")
public final class EtFortnightlyPressListHelper {
    private static final String COURT_ROOM = "courtRoom";
    private static final String SITTING_DATE = "sittingDate";
    private static final String SITTINGS = "sittings";
    private static final String LEGAL_ADVISOR = "legalAdvisor";
    private static final String REP = "rep";
    private static final String SITTING_START = "sittingStart";

    private EtFortnightlyPressListHelper() {
    }

    public static Context preprocessArtefactForEtFortnightlyListThymeLeafConverter(
        JsonNode artefact, Map<String, String> metadata, Map<String, Object> language) {
        Context context;
        context = preprocessArtefactForThymeLeafConverter(artefact, metadata, language, true);
        etFortnightlyListFormatted(artefact, language);
        splitByCourtAndDate(artefact);
        context.setVariable("regionName", metadata.get("regionName"));
        return context;
    }

    public static void splitByCourtAndDate(JsonNode artefact) {
        artefact.get("courtLists").forEach(courtList -> {
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode sittingArray = mapper.createArrayNode();
            Map<Date, String> sittingDateTimes = SittingHelper.findAllSittingDates(
                courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM));
            List<String> uniqueSittingDate = GeneralHelper.findUniqueDateAndSort(sittingDateTimes);
            String[] uniqueSittingDates = uniqueSittingDate.toArray(new String[0]);
            for (int i = 0; i < uniqueSittingDates.length; i++) {
                int finalI = i;
                ObjectNode sittingNode = mapper.createObjectNode();
                ArrayNode hearingNodeArray = mapper.createArrayNode();
                (sittingNode).put(SITTING_DATE, uniqueSittingDates[finalI]);
                courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM).forEach(courtRoom -> {
                    courtRoom.get("session").forEach(session -> {
                        session.get(SITTINGS).forEach(sitting -> {
                            SittingHelper.checkSittingDateAlreadyExists(sitting, uniqueSittingDates,
                                                          hearingNodeArray, finalI);
                        });
                    });
                });
                (sittingNode).putArray("hearing").addAll(hearingNodeArray);
                sittingArray.add(sittingNode);
            }
            ((ObjectNode)courtList).putArray(SITTINGS).addAll(sittingArray);
        });
    }

    public static void etFortnightlyListFormatted(JsonNode artefact, Map<String, Object> language) {
        artefact.get("courtLists").forEach(courtList -> {
            courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM).forEach(courtRoom -> {
                courtRoom.get("session").forEach(session -> {
                    session.get(SITTINGS).forEach(sitting -> {
                        String sittingDate = DateHelper.formatTimeStampToBst(
                            sitting.get(SITTING_START).asText(), Language.ENGLISH, false, false,
                            "EEEE dd MMMM yyyy");
                        ((ObjectNode)sitting).put(SITTING_DATE, sittingDate);
                        sitting.get("hearing").forEach(hearing -> {
                            formatCaseTime(sitting, hearing);
                            moveTableColumnValuesToHearing(courtRoom, sitting, hearing, language);
                            if (hearing.has("case")) {
                                hearing.get("case").forEach(cases -> {
                                    if (!cases.has("caseSequenceIndicator")) {
                                        ((ObjectNode)cases).put("caseSequenceIndicator", "");
                                    }
                                });
                            }
                        });
                    });
                });
            });
        });
    }

    private static void moveTableColumnValuesToHearing(JsonNode courtRoom, JsonNode sitting,
                                                       JsonNode hearing,
                                                       Map<String, Object> language) {
        ((ObjectNode)hearing).put(COURT_ROOM,
            GeneralHelper.findAndReturnNodeText(courtRoom, "courtRoomName"));
        ((ObjectNode)hearing).put("claimant",
            GeneralHelper.findAndReturnNodeText(hearing,"claimant"));
        ((ObjectNode)hearing).put("claimantRepresentative",
            getClaimantRepresentative(GeneralHelper.findAndReturnNodeText(hearing,"claimantRepresentative"),
            language));
        String respondent = GeneralHelper.findAndReturnNodeText(hearing, "respondent");
        ((ObjectNode)hearing).put("respondent",
            findRespondent(respondent, language));
        ((ObjectNode)hearing).put("respondentRepresentative",
            findRespondentRepresentative(respondent,
            language));
        ((ObjectNode)hearing).put("formattedDuration",
            GeneralHelper.findAndReturnNodeText(sitting, "formattedDuration"));
        ((ObjectNode)hearing).put("caseHearingChannel",
            GeneralHelper.findAndReturnNodeText(sitting, "caseHearingChannel"));
    }

    private static String getClaimantRepresentative(String claimantRepresentative,
        Map<String, Object> language) {
        if (claimantRepresentative.isEmpty()) {
            return (String) language.get(REP);
        } else {
            return language.get(REP)
                + claimantRepresentative;
        }
    }

    private static String findRespondent(String respondent,
                                         Map<String, Object> language) {
        String legalAdvisor = (String) language.get(LEGAL_ADVISOR);

        if (respondent.indexOf(legalAdvisor) > 0) {
            return GeneralHelper.trimAnyCharacterFromStringEnd(
                respondent.substring(0, respondent.indexOf(legalAdvisor)));
        }
        return respondent;
    }

    private static String findRespondentRepresentative(String respondentRepresentative,
                                                       Map<String, Object> language) {
        String legalAdvisor = (String) language.get(LEGAL_ADVISOR);

        if (respondentRepresentative.indexOf(legalAdvisor) > 0) {
            return GeneralHelper.trimAnyCharacterFromStringEnd(
                language.get(REP) + respondentRepresentative.substring(respondentRepresentative
                     .indexOf(legalAdvisor) + legalAdvisor.length()));
        }
        if (respondentRepresentative.isEmpty()) {
            return (String) language.get(REP);
        }

        return respondentRepresentative;
    }

    public static void formatCaseTime(JsonNode sitting, JsonNode node) {
        if (!GeneralHelper.findAndReturnNodeText(sitting, SITTING_START).isEmpty()) {
            ((ObjectNode)node).put("time",
                DateHelper.timeStampToBstTime(GeneralHelper
                .findAndReturnNodeText(sitting, SITTING_START), "h:mma"));
        }
    }
}
