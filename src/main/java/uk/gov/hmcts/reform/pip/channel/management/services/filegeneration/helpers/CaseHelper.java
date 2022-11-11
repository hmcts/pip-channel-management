package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class CaseHelper {
    private CaseHelper() {
    }

    /**
     * This method concatenate all case IDs within the linked cases.
     * @param caseInfo The case info that contains the linked cases.
     */
    public static void formatLinkedCases(JsonNode caseInfo) {
        StringBuilder formattedLinked = new StringBuilder();

        if (caseInfo.has("caseLinked")) {
            caseInfo.get("caseLinked").forEach(linkedCase -> {
                if (formattedLinked.length() != 0) {
                    formattedLinked.append(", ");
                }
                formattedLinked.append(GeneralHelper.findAndReturnNodeText(linkedCase, "caseId"));
            });
        }
        ((ObjectNode) caseInfo).put("formattedLinkedCases", formattedLinked.toString());
    }
}
