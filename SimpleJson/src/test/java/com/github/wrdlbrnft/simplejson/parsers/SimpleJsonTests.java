package com.github.wrdlbrnft.simplejson.parsers;

import com.github.wrdlbrnft.simplejson.parsers.models.TestEntities;
import com.github.wrdlbrnft.simplejson.parsers.models.TestEntity;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 04/02/2018
 */
public class SimpleJsonTests {

    private static final Date DATE = new Date(1517700642000L);
    private static final Calendar CALENDAR = Calendar.getInstance();

    static {
        CALENDAR.setTime(DATE);
    }

    private static final TestEntity TEST_ENTITY_A = new TestEntities.Builder()
            .setIntValue(1)
            .setBoxedIntValue(2)
            .setLongValue(3L)
            .setBoxedLongValue(4L)
            .setDoubleValue(5.0)
            .setBoxedDoubleValue(6.0)
            .setFloatValue(7.0f)
            .setBoxedFloatValue(8.0f)
            .setBooleanValue(true)
            .setBoxedBooleanValue(false)
            .setDate(DATE)
            .setCalendar(CALENDAR)
            .setCalendarPattern(CALENDAR)
            .setCalendarUnixSeconds(CALENDAR)
            .setCalendarUnixMilliSeconds(CALENDAR)
            .setStringValue("asdf")
            .setStatus(TestEntity.Status.A)
            .setStatuses(Arrays.asList(TestEntity.Status.B, TestEntity.Status.C))
            .setStatusesSet(EnumSet.of(TestEntity.Status.B, TestEntity.Status.C))
            .setOther(null)
            .setOthers(Collections.<TestEntity>emptyList())
            .build();

    private static final TestEntity TEST_ENTITY_B = new TestEntities.Builder()
            .setIntValue(10)
            .setBoxedIntValue(20)
            .setLongValue(30L)
            .setBoxedLongValue(40L)
            .setDoubleValue(50.0)
            .setBoxedDoubleValue(60.0)
            .setFloatValue(70.0f)
            .setBoxedFloatValue(80.0f)
            .setBooleanValue(false)
            .setBoxedBooleanValue(false)
            .setDate(DATE)
            .setCalendar(CALENDAR)
            .setCalendarPattern(CALENDAR)
            .setCalendarUnixSeconds(CALENDAR)
            .setCalendarUnixMilliSeconds(CALENDAR)
            .setStringValue("asdasdasdasdf")
            .setStatus(TestEntity.Status.B)
            .setStatuses(Arrays.asList(TestEntity.Status.A, TestEntity.Status.C))
            .setStatusesSet(EnumSet.of(TestEntity.Status.A, TestEntity.Status.C))
            .setOther(TEST_ENTITY_A)
            .setOthers(Collections.singletonList(TEST_ENTITY_A))
            .build();

    private static final String TEST_JSON = "{\n" +
            "  \"date\": 1517700642000,\n" +
            "  \"date_test_seconds\": 1517700642,\n" +
            "  \"boxed_boolean_value\": false,\n" +
            "  \"other\": {\n" +
            "    \"date\": 1517700642000,\n" +
            "    \"date_test_seconds\": 1517700642,\n" +
            "    \"boxed_boolean_value\": false,\n" +
            "    \"boxed_int_value\": 2,\n" +
            "    \"string_value\": \"asdf\",\n" +
            "    \"boolean_value\": true,\n" +
            "    \"boxed_float_value\": 8,\n" +
            "    \"date_test\": \"04/02/2018 12:30:42 +0100\",\n" +
            "    \"date_test_milli_seconds\": 1517700642000,\n" +
            "    \"double_value\": 5,\n" +
            "    \"int_value\": 1,\n" +
            "    \"status_set\": [\n" +
            "      \"jklö\",\n" +
            "      \"test\"\n" +
            "    ],\n" +
            "    \"boxed_double_value\": 6,\n" +
            "    \"float_value\": 7,\n" +
            "    \"boxed_long_value\": 4,\n" +
            "    \"long_value\": 3,\n" +
            "    \"status_list\": [\n" +
            "      \"jklö\",\n" +
            "      \"test\"\n" +
            "    ],\n" +
            "    \"others\": [],\n" +
            "    \"status\": \"asdf\"\n" +
            "  },\n" +
            "  \"boxed_int_value\": 20,\n" +
            "  \"string_value\": \"asdasdasdasdf\",\n" +
            "  \"boolean_value\": false,\n" +
            "  \"boxed_float_value\": 80,\n" +
            "  \"date_test\": \"04/02/2018 12:30:42 +0100\",\n" +
            "  \"date_test_milli_seconds\": 1517700642000,\n" +
            "  \"double_value\": 50,\n" +
            "  \"int_value\": 10,\n" +
            "  \"status_set\": [\n" +
            "    \"asdf\",\n" +
            "    \"test\"\n" +
            "  ],\n" +
            "  \"boxed_double_value\": 60,\n" +
            "  \"float_value\": 70,\n" +
            "  \"boxed_long_value\": 40,\n" +
            "  \"long_value\": 30,\n" +
            "  \"status_list\": [\n" +
            "    \"asdf\",\n" +
            "    \"test\"\n" +
            "  ],\n" +
            "  \"others\": [\n" +
            "    {\n" +
            "      \"date\": 1517700642000,\n" +
            "      \"date_test_seconds\": 1517700642,\n" +
            "      \"boxed_boolean_value\": false,\n" +
            "      \"boxed_int_value\": 2,\n" +
            "      \"string_value\": \"asdf\",\n" +
            "      \"boolean_value\": true,\n" +
            "      \"boxed_float_value\": 8,\n" +
            "      \"date_test\": \"04/02/2018 12:30:42 +0100\",\n" +
            "      \"date_test_milli_seconds\": 1517700642000,\n" +
            "      \"double_value\": 5,\n" +
            "      \"int_value\": 1,\n" +
            "      \"status_set\": [\n" +
            "        \"jklö\",\n" +
            "        \"test\"\n" +
            "      ],\n" +
            "      \"boxed_double_value\": 6,\n" +
            "      \"float_value\": 7,\n" +
            "      \"boxed_long_value\": 4,\n" +
            "      \"long_value\": 3,\n" +
            "      \"status_list\": [\n" +
            "        \"jklö\",\n" +
            "        \"test\"\n" +
            "      ],\n" +
            "      \"others\": [],\n" +
            "      \"status\": \"asdf\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"status\": \"jklö\"\n" +
            "}";

    @Test
    public void parseTest() {
        final TestEntity parsedEntity = TestEntities.fromJson(TEST_JSON);
        Assert.assertThat(parsedEntity, CoreMatchers.equalTo(TEST_ENTITY_B));
    }
}