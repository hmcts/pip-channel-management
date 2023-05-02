package uk.gov.hmcts.reform.pip.channel.management.services.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
class GeneralHelperTest {
    private static final String ERR_MSG = "Helper method doesn't seem to be working correctly";
    private static final String TEST = "test";

    private static final String DOCUMENT = "document";
    private static JsonNode inputJson;

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/familyDailyCauseList.json")), writer,
                     Charset.defaultCharset()
        );

        inputJson = new ObjectMapper().readTree(writer.toString());
    }

    @Test
    void testStringDelimiterWithEmptyStringMethod() {
        assertThat(GeneralHelper.stringDelimiter("", ","))
            .as(ERR_MSG)
            .isEmpty();
    }

    @Test
    void testStringDelimiterWithoutEmptyStringMethod() {
        assertThat(GeneralHelper.stringDelimiter(TEST, ","))
            .as(ERR_MSG)
            .isEqualTo(",");
    }

    @Test
    void testFindAndReturnNodeTextMethod() {
        assertThat(GeneralHelper.findAndReturnNodeText(inputJson.get(DOCUMENT), "publicationDate"))
            .as(ERR_MSG)
            .isEqualTo("2022-07-21T14:01:43Z");
    }

    @Test
    void testFindAndReturnNodeTextNotExistsMethod() {
        assertThat(GeneralHelper.findAndReturnNodeText(inputJson.get(DOCUMENT), TEST))
            .as(ERR_MSG)
            .isEmpty();
    }

    @Test
    void testFindAndReturnNodeTextWheNodeNotExistsMethod() {
        assertThat(GeneralHelper.findAndReturnNodeText(inputJson.get(TEST), "publicationDate"))
            .as(ERR_MSG)
            .isEmpty();
    }

    @Test
    void testTrimAnyCharacterFromStringEndMethod() {
        assertThat(GeneralHelper.trimAnyCharacterFromStringEnd("test,"))
            .as(ERR_MSG)
            .isEqualTo(TEST);
    }

    @Test
    void testTrimAnyCharacterFromStringWithSpaceEndMethod() {
        assertThat(GeneralHelper.trimAnyCharacterFromStringEnd("test, "))
            .as(ERR_MSG)
            .isEqualTo(TEST);
    }

    @Test
    void testAppendToStringBuilderMethod() {
        StringBuilder builder = new StringBuilder();
        builder.append("Test1");
        GeneralHelper.appendToStringBuilder(builder,"Test2 ", inputJson.get("venue"),
                                            "venueName");
        assertThat(builder)
            .as(ERR_MSG)
            .hasToString("Test1\nTest2 This is the venue name");
    }

    @Test
    void testAppendToStringBuilderWithPrefix() {
        StringBuilder builder = new StringBuilder();
        builder.append("Test1");
        GeneralHelper.appendToStringBuilderWithPrefix(builder,"Test2: ", inputJson.get("venue"),
                                                      "venueName", "\t\t");
        assertThat(builder)
            .as(ERR_MSG)
            .hasToString("Test1\t\tTest2: This is the venue name");
    }

    @Test
    void testLoopAndFormatString() {
        StringBuilder builder = new StringBuilder();
        JsonNode node = inputJson.get("venue").get("venueAddress");
        GeneralHelper.loopAndFormatString(node, "line", builder, ",");
        assertThat(builder)
            .as(ERR_MSG)
            .hasToString("Address Line 1,");
    }

    @Test
    void testSafeGetWithReturningResult() {
        assertThat(GeneralHelper.safeGet("publicationDate", inputJson.get(DOCUMENT)))
            .as(ERR_MSG)
            .isEqualTo("2022-07-21T14:01:43Z");
    }

    @Test
    void testSafeGetWithReturningEmpty() {
        assertThat(GeneralHelper.safeGet("Test", inputJson.get(DOCUMENT)))
            .as(ERR_MSG)
            .isEmpty();
    }

    @Test
    void testSafeGetWithReturningEmptyForBullPointerException() {
        assertThat(GeneralHelper.safeGet("Test.0", inputJson.get(DOCUMENT)))
            .as(ERR_MSG)
            .isEmpty();
    }
}
