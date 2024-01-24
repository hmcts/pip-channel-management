package uk.gov.hmcts.reform.pip.channel.management.services.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class JudiciaryHelper {
    private static final String JUDICIARY = "judiciary";

    private JudiciaryHelper() {
    }

    public static String findAndManipulateJudiciary(JsonNode judiciaryNode) {
        AtomicReference<StringBuilder> presidingJudiciary = new AtomicReference<>(new StringBuilder());
        List<String> judiciaries = new ArrayList<>();

        if (judiciaryNode.has(JUDICIARY)) {
            judiciaryNode.get(JUDICIARY).forEach(judiciary -> {
                String johKnownAs = GeneralHelper.findAndReturnNodeText(judiciary, "johKnownAs");
                if ("true".equals(GeneralHelper.findAndReturnNodeText(judiciary, "isPresiding"))) {
                    presidingJudiciary.set(new StringBuilder(johKnownAs));
                } else {
                    judiciaries.add(johKnownAs);
                }
            });
        }

        if (StringUtils.isNotBlank(presidingJudiciary.get())) {
            judiciaries.add(0, String.valueOf(presidingJudiciary.get()));
        }

        return String.join(", ", judiciaries);
    }

    public static String findAndManipulateJudiciaryForCrime(JsonNode judiciaryNode) {
        AtomicReference<StringBuilder> presidingJudiciary = new AtomicReference<>(new StringBuilder());
        List<String> judiciaries = new ArrayList<>();

        if (judiciaryNode.has(JUDICIARY)) {
            judiciaryNode.get(JUDICIARY).forEach(judiciary -> {
                String johTitle = GeneralHelper.findAndReturnNodeText(judiciary, "johTitle");
                String johNameSurname = GeneralHelper.findAndReturnNodeText(judiciary, "johNameSurname");
                String judgeName = johTitle + " " + johNameSurname;
                if ("true".equals(GeneralHelper.findAndReturnNodeText(judiciary, "isPresiding"))) {
                    presidingJudiciary.set(new StringBuilder(judgeName));
                } else {
                    judiciaries.add(judgeName);
                }
            });
        }

        if (StringUtils.isNotBlank(presidingJudiciary.get())) {
            judiciaries.add(0, String.valueOf(presidingJudiciary.get()));
        }

        return String.join(", ", judiciaries);
    }
}
