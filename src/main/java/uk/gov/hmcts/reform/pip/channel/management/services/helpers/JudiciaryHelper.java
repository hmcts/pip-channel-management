package uk.gov.hmcts.reform.pip.channel.management.services.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicReference;

public final class JudiciaryHelper {
    private static final String JUDICIARY = "judiciary";

    private JudiciaryHelper() {
    }

    public static String findAndManipulateJudiciary(JsonNode judiciaryNode) {
        AtomicReference<StringBuilder> nonPresidingJudiciary = new AtomicReference<>(new StringBuilder());
        AtomicReference<StringBuilder> presidingJudiciary = new AtomicReference<>(new StringBuilder());
        AtomicReference<Boolean> foundPresiding = new AtomicReference<>(false);
        AtomicReference<Boolean> foundNonPresidingJudges = new AtomicReference<>(false);

        if (judiciaryNode.has(JUDICIARY)) {
            judiciaryNode.get(JUDICIARY).forEach(judiciary -> {
                if ("true".equals(GeneralHelper.findAndReturnNodeText(judiciary, "isPresiding"))) {
                    appendJohKnownAs(judiciary, presidingJudiciary.get());
                    foundPresiding.set(true);
                } else {
                    appendJohKnownAs(judiciary, nonPresidingJudiciary.get());
                    foundNonPresidingJudges.set(true);
                }
            });
        }

        if (foundPresiding.get() && foundNonPresidingJudges.get()) {
            return GeneralHelper.trimAnyCharacterFromStringEnd(
                presidingJudiciary.get().append(nonPresidingJudiciary.get()).toString());
        } else if (foundPresiding.get()) {
            return GeneralHelper.trimAnyCharacterFromStringEnd(presidingJudiciary.toString());
        } else {
            return GeneralHelper.trimAnyCharacterFromStringEnd(nonPresidingJudiciary.toString());
        }
    }

    private static void appendJohKnownAs(JsonNode judiciary, StringBuilder judiciaryKnownAs) {
        String johKnownAs = GeneralHelper.findAndReturnNodeText(judiciary, "johKnownAs");
        if (StringUtils.isNotBlank(johKnownAs)) {
            judiciaryKnownAs
                .append(johKnownAs)
                .append(", ");
        }
    }
}
