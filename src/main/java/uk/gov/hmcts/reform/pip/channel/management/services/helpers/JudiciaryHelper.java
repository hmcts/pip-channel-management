package uk.gov.hmcts.reform.pip.channel.management.services.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicReference;

public final class JudiciaryHelper {
    private static final String JUDICIARY = "judiciary";

    private JudiciaryHelper() {
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
                    appendJohKnownAs(judiciary, formattedJudiciary.get());
                }
            });

            if (!GeneralHelper.trimAnyCharacterFromStringEnd(formattedJudiciary.toString()).isEmpty()
                && addBeforeToJudgeName) {
                formattedJudiciary.get().insert(0, "Before: ");
            }
        }

        return GeneralHelper.trimAnyCharacterFromStringEnd(formattedJudiciary.toString());
    }

    public static String findAndManipulateJudiciaryForIac(JsonNode session) {
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

    private static void appendJohKnownAs(JsonNode judiciary, StringBuilder formattedJudiciary) {
        String johKnownAs = GeneralHelper.findAndReturnNodeText(judiciary, "johKnownAs");
        if (StringUtils.isNotBlank(johKnownAs)) {
            formattedJudiciary
                .append(johKnownAs)
                .append(", ");
        }
    }
}
