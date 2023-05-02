package uk.gov.hmcts.reform.pip.channel.management.services.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class CaseHelper {
    private static final String CASE_SEQUENCE_INDICATOR = "caseSequenceIndicator";

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
}
