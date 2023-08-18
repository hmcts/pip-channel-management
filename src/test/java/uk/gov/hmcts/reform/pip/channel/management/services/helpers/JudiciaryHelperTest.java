package uk.gov.hmcts.reform.pip.channel.management.services.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JudiciaryHelperTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String JUDICIARY_MESSAGE = "Judiciary does not match";

    private static JsonNode inputJson;

    @BeforeAll
    static void setup() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(
                         Paths.get("src/test/resources/mocks/judiciaryManipulation.json")), writer,
                     Charset.defaultCharset()
        );
        inputJson = OBJECT_MAPPER.readTree(writer.toString());
    }

    @Test
    void testJudiciaryManipulationWithNoPresidingJudge() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciary(inputJson.get(0), Optional.empty()))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Judge 1, Judge 2, Judge 3");
    }

    @Test
    void testJudiciaryManipulationWithMissingPresidingJudge() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciary(inputJson.get(1), Optional.empty()))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Judge 1, Judge 2, Judge 3");
    }

    @Test
    void testJudiciaryManipulationWithAPresidingJudge() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciary(inputJson.get(2), Optional.empty()))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Crown Judge");
    }

    @Test
    void testJudiciaryManipulationWithBeforeAddedToJudgeNameInEnglish() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciary(inputJson.get(2), Optional.of(Language.ENGLISH)))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Before: Crown Judge");
    }

    @Test
    void testJudiciaryManipulationWithBeforeAddedToJudgeNameInWelsh() {
        assertThat(JudiciaryHelper.findAndManipulateJudiciary(inputJson.get(2), Optional.of(Language.WELSH)))
            .as(JUDICIARY_MESSAGE)
            .isEqualTo("Gerbron: Crown Judge");
    }
}