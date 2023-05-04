package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.SittingHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public final class EtFortnightlyPressListHelper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SITTING_DATE = "sittingDate";
    private static final String SITTINGS = "sittings";
    private static final String REP = "rep";
    private static final String SITTING_START = "sittingStart";

    private EtFortnightlyPressListHelper() {
    }

    public static void splitByCourtAndDate(JsonNode artefact) {
        artefact.get("courtLists").forEach(courtList -> {
            ArrayNode sittingArray = MAPPER.createArrayNode();
            Map<Date, String> sittingDateTimes = SittingHelper.findAllSittingDates(
                courtList.get(COURT_HOUSE).get(COURT_ROOM));
            List<String> uniqueSittingDate = GeneralHelper.findUniqueDateAndSort(sittingDateTimes);
            String[] uniqueSittingDates = uniqueSittingDate.toArray(new String[0]);

            for (int i = 0; i < uniqueSittingDates.length; i++) {
                int currentSittingDate = i;
                ObjectNode sittingNode = MAPPER.createObjectNode();
                ArrayNode hearingNodeArray = MAPPER.createArrayNode();
                (sittingNode).put(SITTING_DATE, uniqueSittingDates[currentSittingDate]);
                courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                    courtRoom -> courtRoom.get("session").forEach(
                        session -> session.get(SITTINGS).forEach(sitting -> {
                            (sittingNode).put("time", sitting.get("time").asText());
                            SittingHelper.checkSittingDateAlreadyExists(sitting, uniqueSittingDates,
                                                          hearingNodeArray, currentSittingDate);
                        })
                    )
                );
                (sittingNode).putArray("hearing").addAll(hearingNodeArray);
                sittingArray.add(sittingNode);
            }
            ((ObjectNode)courtList).putArray(SITTINGS).addAll(sittingArray);
        });
    }

    public static void etFortnightlyListFormatted(JsonNode artefact, Map<String, Object> language) {
        artefact.get("courtLists").forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get(SITTINGS).forEach(sitting -> {
                        String sittingDate = DateHelper.formatTimeStampToBst(
                            sitting.get(SITTING_START).asText(), Language.ENGLISH, false, false,
                            "EEEE dd MMMM yyyy");
                        ((ObjectNode)sitting).put(SITTING_DATE, sittingDate);
                        DateHelper.formatStartTime(sitting,"h:mma", true);
                        sitting.get("hearing").forEach(hearing -> {
                            moveTableColumnValuesToHearing(courtRoom, sitting, (ObjectNode) hearing, language);
                            if (hearing.has("case")) {
                                hearing.get("case").forEach(cases -> {
                                    if (!cases.has("caseSequenceIndicator")) {
                                        ((ObjectNode)cases).put("caseSequenceIndicator", "");
                                    }
                                });
                            }
                        });
                    })
                )
            )
        );
    }

    private static void moveTableColumnValuesToHearing(JsonNode courtRoom, JsonNode sitting,
                                                       ObjectNode hearing,
                                                       Map<String, Object> language) {
        hearing.put(COURT_ROOM,
                    GeneralHelper.findAndReturnNodeText(courtRoom, "courtRoomName"));
        hearing.put("claimant",
                    GeneralHelper.findAndReturnNodeText(hearing,"claimant"));
        hearing.put("claimantRepresentative",
                    language.get(REP) + GeneralHelper.findAndReturnNodeText(hearing, "claimantRepresentative"));
        hearing.put("respondent",
                    GeneralHelper.findAndReturnNodeText(hearing, "respondent"));
        hearing.put("respondentRepresentative",
                    language.get(REP) + GeneralHelper.findAndReturnNodeText(hearing, "respondentRepresentative"));
        hearing.put("formattedDuration",
                    GeneralHelper.findAndReturnNodeText(sitting, "formattedDuration"));
        hearing.put("caseHearingChannel",
                    GeneralHelper.findAndReturnNodeText(sitting, "caseHearingChannel"));
    }
}
