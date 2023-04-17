package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.util.StringUtils;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.concurrent.atomic.AtomicReference;

public final class DataManipulation {
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";
    private static final String TIME_FORMAT = "h:mma";

    private static final String CHANNEL = "channel";
    private static final String JUDICIARY = "judiciary";
    private static final String APPLICANT = "applicant";
    private static final String RESPONDENT = "respondent";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String SESSION_CHANNEL = "sessionChannel";

    private DataManipulation() {
        throw new UnsupportedOperationException();
    }

    public static void manipulatedDailyListData(JsonNode artefact, Language language, boolean initialised) {
        artefact.get("courtLists").forEach(
            courtList -> courtList.get(LocationHelper.COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(session -> {
                    StringBuilder formattedJudiciary = new StringBuilder();
                    formattedJudiciary.append(findAndManipulateJudiciary(session));
                    session.get(SITTINGS).forEach(sitting -> {
                        DateHelper.calculateDuration(sitting, language);
                        DateHelper.formatStartTime(sitting, TIME_FORMAT, true);
                        findAndConcatenateHearingPlatform(sitting, session);

                        sitting.get(HEARING).forEach(hearing -> {
                            if (hearing.has("party")) {
                                PartyRoleHelper.findAndManipulatePartyInformation(hearing, initialised);
                            } else {
                                ((ObjectNode) hearing).put(APPLICANT, "");
                                ((ObjectNode) hearing).put(RESPONDENT, "");
                            }
                            hearing.get("case").forEach(DataManipulation::manipulateCaseInformation);
                        });
                    });
                    LocationHelper.formattedCourtRoomName(courtRoom, session, formattedJudiciary);
                })
            )
        );
    }

    public static void manipulateCaseInformation(JsonNode hearingCase) {
        if (!GeneralHelper.findAndReturnNodeText(hearingCase, CASE_SEQUENCE_INDICATOR).isEmpty()) {
            ((ObjectNode) hearingCase).put(
                "caseName",
                GeneralHelper.findAndReturnNodeText(hearingCase, "caseName")
                    + " " + hearingCase.get(CASE_SEQUENCE_INDICATOR).asText()
            );
        }

        if (!hearingCase.has("caseType")) {
            ((ObjectNode) hearingCase).put("caseType", "");
        }
    }

    public static void findAndConcatenateHearingPlatform(JsonNode sitting, JsonNode session) {
        StringBuilder formattedHearingPlatform = new StringBuilder();

        if (sitting.has(CHANNEL)) {
            GeneralHelper.loopAndFormatString(sitting, CHANNEL,
                                              formattedHearingPlatform, ", "
            );
        } else if (session.has(SESSION_CHANNEL)) {
            GeneralHelper.loopAndFormatString(session, SESSION_CHANNEL,
                                              formattedHearingPlatform, ", "
            );
        }

        ((ObjectNode) sitting).put("caseHearingChannel", GeneralHelper.trimAnyCharacterFromStringEnd(
            formattedHearingPlatform.toString().trim()));
    }

    public static String findAndManipulateJudiciaryForCop(JsonNode session) {
        StringBuilder formattedJudiciary = new StringBuilder();

        try {
            session.get(JUDICIARY).forEach(judiciary -> {
                if (formattedJudiciary.length() != 0) {
                    formattedJudiciary.append(", ");
                }

                formattedJudiciary.append(GeneralHelper.findAndReturnNodeText(judiciary, "johTitle"));
                formattedJudiciary.append(' ');
                formattedJudiciary.append(GeneralHelper.findAndReturnNodeText(judiciary, "johNameSurname"));
            });

        } catch (Exception ignored) {
            //No catch required, this is a valid scenario and makes the code cleaner than many if statements
        }

        return GeneralHelper.trimAnyCharacterFromStringEnd(formattedJudiciary.toString());
    }

    public static String findAndManipulateJudiciary(JsonNode judiciaryNode) {
        return findAndManipulateJudiciary(judiciaryNode, true);
    }

    public static String findAndManipulateJudiciary(JsonNode judiciaryNode, boolean addBeforeToJudgeName) {
        AtomicReference<StringBuilder> formattedJudiciary = new AtomicReference<>(new StringBuilder());
        AtomicReference<Boolean> foundPresiding = new AtomicReference<>(false);

        if (judiciaryNode.has(JUDICIARY)) {
            judiciaryNode.get(JUDICIARY).forEach(judiciary -> {
                if ("true".equals(GeneralHelper.findAndReturnNodeText(judiciary, "isPresiding"))) {
                    formattedJudiciary.set(new StringBuilder());
                    formattedJudiciary.get().append(GeneralHelper.findAndReturnNodeText(judiciary, "johKnownAs"));
                    foundPresiding.set(true);
                } else if (Boolean.FALSE.equals(foundPresiding.get())) {
                    String johKnownAs = GeneralHelper.findAndReturnNodeText(judiciary, "johKnownAs");
                    if (StringUtils.isNotBlank(johKnownAs)) {
                        formattedJudiciary.get()
                            .append(johKnownAs)
                            .append(", ");
                    }
                }
            });

            if (!GeneralHelper.trimAnyCharacterFromStringEnd(formattedJudiciary.toString()).isEmpty()
                    && addBeforeToJudgeName) {
                formattedJudiciary.get().insert(0, "Before: ");
            }
        }

        return GeneralHelper.trimAnyCharacterFromStringEnd(formattedJudiciary.toString());
    }
}
