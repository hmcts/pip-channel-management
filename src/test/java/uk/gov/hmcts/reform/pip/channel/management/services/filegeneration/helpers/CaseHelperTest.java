package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class CaseHelperTest {
    private static final String CASE_ID = "caseId";
    private static final String CASE_NUMBER = "caseNumber";
    private static final String CASE_LINKED = "caseLinked";
    private static final String FORMATTED_LINKED_CASES = "formattedLinkedCases";
    private static final String ERROR_MESSAGE = "Linked cases do not match";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testFormatMultipleLinkedCases() {
        ObjectNode caseIdNode1 = MAPPER.createObjectNode();
        caseIdNode1.put(CASE_ID, "123");

        ObjectNode caseIdNode2 = MAPPER.createObjectNode();
        caseIdNode2.put(CASE_ID, "456");

        ObjectNode caseIdNode3 = MAPPER.createObjectNode();
        caseIdNode3.put(CASE_ID, "789");

        ArrayNode caseIdArrayNode = MAPPER.createArrayNode();
        caseIdArrayNode.add(caseIdNode1);
        caseIdArrayNode.add(caseIdNode2);
        caseIdArrayNode.add(caseIdNode3);

        ObjectNode linkedCasesNode = MAPPER.createObjectNode();
        linkedCasesNode.put(CASE_LINKED, caseIdArrayNode);

        ArrayNode caseNode = MAPPER.createArrayNode();
        caseNode.add(linkedCasesNode);

        CaseHelper.formatLinkedCases(caseNode.get(0));
        assertThat(caseNode.get(0).get(FORMATTED_LINKED_CASES).asText())
            .as(ERROR_MESSAGE)
            .isEqualTo("123, 456, 789");
    }

    @Test
    void testFormatSingleLinkedCase() {
        ObjectNode caseIdNode = MAPPER.createObjectNode();
        caseIdNode.put(CASE_ID, "999");

        ArrayNode caseIdArrayNode = MAPPER.createArrayNode();
        caseIdArrayNode.add(caseIdNode);

        ObjectNode linkedCasesNode = MAPPER.createObjectNode();
        linkedCasesNode.put(CASE_LINKED, caseIdArrayNode);

        ArrayNode caseNode = MAPPER.createArrayNode();
        caseNode.add(linkedCasesNode);

        CaseHelper.formatLinkedCases(caseNode.get(0));
        assertThat(caseNode.get(0).get(FORMATTED_LINKED_CASES).asText())
            .as(ERROR_MESSAGE)
            .isEqualTo("999");
    }

    @Test
    void testFormatEmptyLinkedCase() {
        ObjectNode caseNumberNode = MAPPER.createObjectNode();
        caseNumberNode.put(CASE_NUMBER, "999");

        ArrayNode caseNode = MAPPER.createArrayNode();
        caseNode.add(caseNumberNode);

        CaseHelper.formatLinkedCases(caseNode.get(0));
        assertThat(caseNode.get(0).get(FORMATTED_LINKED_CASES).asText())
            .as(ERROR_MESSAGE)
            .isEmpty();
    }
}
