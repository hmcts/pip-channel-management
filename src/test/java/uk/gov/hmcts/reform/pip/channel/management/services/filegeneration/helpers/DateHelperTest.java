package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
class DateHelperTest {

    private static final String ERR_MSG = "Helper method doesn't seem to be working correctly";
    private static final String TEST_DATETIME_1 = "2022-08-19T09:30:00Z";
    private static final String TEST_DATETIME_2 = "2022-07-26T16:04:43.416924Z";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testLocalTimeMethod() {
        assertThat(DateHelper.formatLocalDateTimeToBst(
            LocalDateTime.of(1988, Month.SEPTEMBER, 29, 8, 30)))
            .as(ERR_MSG)
            .isEqualTo("29 September 1988");
    }

    @Test
    void testZonedDateMethod() {
        assertThat(DateHelper.formatTimeStampToBst(
            TEST_DATETIME_2, Language.ENGLISH, false, false))
            .as(ERR_MSG)
            .isEqualTo("26 July 2022");
    }

    @Test
    void testZonedDateMethodWithDateFormat() {
        assertThat(DateHelper.formatTimeStampToBst(
            TEST_DATETIME_2, Language.ENGLISH, false, false, "dd MMMM"))
            .as(ERR_MSG)
            .isEqualTo("26 July");
    }

    @Test
    void testZonedTimeOnlyHoursMethod() {
        assertThat(DateHelper.formatTimeStampToBst(
            "2022-07-26T16:00:00.416924Z", Language.ENGLISH, true, false))
            .as(ERR_MSG)
            .contains("5");
    }

    @Test
    void testZonedTimeOnlyTwoDigitsHoursMethod() {
        assertThat(DateHelper.formatTimeStampToBst(
            "2022-07-26T22:00:00.416924Z", Language.ENGLISH, true, false))
            .as(ERR_MSG)
            .contains("11");
    }

    @Test
    void testZonedDateTimeMethod() {
        assertThat(DateHelper.formatTimeStampToBst(
            TEST_DATETIME_2,Language.ENGLISH, false, true))
            .as(ERR_MSG)
            .isEqualTo("26 July 2022 at 17:04");
    }

    @Test
    void testConvertStringToBstMethod() {
        assertThat(DateHelper.convertStringToBst(TEST_DATETIME_1).toLocalDateTime())
            .as(ERR_MSG)
            .isEqualTo("2022-08-19T10:30");
    }

    @Test
    void testConvertTimeToMinutesMethod() {
        ZonedDateTime startTime = DateHelper.convertStringToBst(TEST_DATETIME_1);
        ZonedDateTime endTime = DateHelper.convertStringToBst("2022-08-19T10:55:00Z");

        assertThat(DateHelper.convertTimeToMinutes(startTime, endTime))
            .as(ERR_MSG)
            .isEqualTo(85);
    }

    @Test
    void testFormatDurationInDaysForSingleDay() {
        assertThat(DateHelper.formatDurationInDays(1, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("1 day");
    }

    @Test
    void testFormatDurationInDaysForMultipleDays() {
        assertThat(DateHelper.formatDurationInDays(3, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("3 days");
    }

    @Test
    void testFormatDurationInDaysInNonEnglish() {
        assertThat(DateHelper.formatDurationInDays(1, Language.WELSH))
            .as(ERR_MSG)
            .isEqualTo("1 dydd/day");
    }

    @Test
    void testFormatDurationMethod() {
        assertThat(DateHelper.formatDuration(3, 10, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("3 hours 10 mins");
    }

    @Test
    void testFormatDurationMethodInNonEnglish() {
        assertThat(DateHelper.formatDuration(3, 10, Language.WELSH))
            .as(ERR_MSG)
            .isEqualTo("3 awr/hours 10 munud/minutes");
    }

    @Test
    void testFormatDurationWithNoMinutesMethod() {
        assertThat(DateHelper.formatDuration(3, 0, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("3 hours");
    }

    @Test
    void testFormatDurationWithSingleHourNoMinutesMethod() {
        assertThat(DateHelper.formatDuration(1, 0, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("1 hour");
    }

    @Test
    void testFormatDurationWithNoHourMethod() {
        assertThat(DateHelper.formatDuration(0, 30, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("30 mins");
    }

    @Test
    void testFormatDurationWithNoHourAndOneMinuteMethod() {
        assertThat(DateHelper.formatDuration(0, 1, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("1 min");
    }

    @Test
    void testFormatDurationWithSingleMinuteNoHourMethod() {
        assertThat(DateHelper.formatDuration(0, 1, Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("1 min");
    }

    @Test
    void testFormatDurationWithNoMinuteNoHourMethod() {
        assertThat(DateHelper.formatDuration(0, 0, Language.ENGLISH))
            .as(ERR_MSG)
            .isEmpty();
    }

    @Test
    void testTimeStampToBstTimeMethod() {
        assertThat(DateHelper.timeStampToBstTime(TEST_DATETIME_1))
            .as(ERR_MSG)
            .isEqualTo("10:30");
    }

    @Test
    void testTimeStampToBstTimeForAfternoonTimeMethod() {
        assertThat(DateHelper.timeStampToBstTime("2022-08-19T13:30:00Z"))
            .as(ERR_MSG)
            .isEqualTo("14:30");
    }

    @Test
    void testTimeStampToBstTimeWithFormatForAmMethod() {
        assertThat(DateHelper.timeStampToBstTimeWithFormat(TEST_DATETIME_1, "h:mma"))
            .as(ERR_MSG)
            .isEqualTo("10:30am");
    }

    @Test
    void testTimeStampToBstTimeWithFormatForPmMethod() {
        assertThat(DateHelper.timeStampToBstTimeWithFormat("2022-08-19T13:30:00Z", "h:mma"))
            .as(ERR_MSG)
            .isEqualTo("2:30pm");
    }

    @Test
    void testTimestampToBstTimeWithNoFormat() {
        assertThat(DateHelper.timeStampToBstTime("2022-08-19T10:30:00Z"))
            .as(ERR_MSG)
            .isEqualTo("11:30");
    }

    @Test
    void testTimestampToBstTimeWithFormat() {
        assertThat(DateHelper.timeStampToBstTimeWithFormat("2022-08-19T10:30:00Z", "hh:mma"))
            .as(ERR_MSG)
            .isEqualTo("11:30am");
    }

    @Test
    void testFormatTimeStampToBstHavingWeekDay() {
        assertThat(DateHelper.formatTimeStampToBstHavingWeekDay(
            TEST_DATETIME_2, "dd MMMM yyyy", Language.ENGLISH))
            .as(ERR_MSG)
            .isEqualTo("Tuesday 26 July 2022");
    }

    @Test
    void testCalculateDurationInDays() {
        ObjectNode sittingNode = MAPPER.createObjectNode();
        sittingNode.put("sittingStart", "2022-12-10T10:00:52.123Z");
        sittingNode.put("sittingEnd", "2022-12-12T12:30:52.123Z");

        DateHelper.calculateDuration(sittingNode, Language.ENGLISH, true);
        assertThat(sittingNode.get("formattedDuration").asText())
            .as(ERR_MSG)
            .isEqualTo("2 days");
    }

    @Test
    void testCalculateDurationInHoursAndMinutes() {
        ObjectNode sittingNode = MAPPER.createObjectNode();
        sittingNode.put("sittingStart", "2022-12-10T10:00:52.123Z");
        sittingNode.put("sittingEnd", "2022-12-10T12:30:52.123Z");

        DateHelper.calculateDuration(sittingNode, Language.ENGLISH, true);
        assertThat(sittingNode.get("formattedDuration").asText())
            .as(ERR_MSG)
            .isEqualTo("2 hours 30 mins");
    }

    @Test
    void testFormatStartTime() {
        ObjectNode sittingNode = MAPPER.createObjectNode();
        sittingNode.put("sittingStart", "2022-12-10T15:30:52.123Z");

        DateHelper.formatStartTime(sittingNode, "h:mma");
        assertThat(sittingNode.get("time").asText())
            .as(ERR_MSG)
            .isEqualTo("3:30pm");
    }
}
