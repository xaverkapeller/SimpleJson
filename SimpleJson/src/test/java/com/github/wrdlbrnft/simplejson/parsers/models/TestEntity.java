package com.github.wrdlbrnft.simplejson.parsers.models;

import com.github.wrdlbrnft.simplejson.annotations.DatePattern;
import com.github.wrdlbrnft.simplejson.annotations.FieldName;
import com.github.wrdlbrnft.simplejson.annotations.JsonEntity;
import com.github.wrdlbrnft.simplejson.annotations.JsonEnum;
import com.github.wrdlbrnft.simplejson.annotations.MapDefault;
import com.github.wrdlbrnft.simplejson.annotations.MapTo;
import com.github.wrdlbrnft.simplejson.annotations.Optional;
import com.github.wrdlbrnft.simplejson.annotations.UnixTimeStamp;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 28/01/2018
 */
@JsonEntity
public interface TestEntity {

    @JsonEnum
    enum Status {
        @MapTo("asdf")
        A,
        @MapTo("jklö")
        B,
        @MapDefault
        @MapTo("test")
        C
    }

    @FieldName("int_value")
    int getIntValue();

    @FieldName("boxed_int_value")
    Integer getBoxedIntValue();

    @FieldName("long_value")
    long getLongValue();

    @FieldName("boxed_long_value")
    Long getBoxedLongValue();

    @FieldName("double_value")
    double getDoubleValue();

    @FieldName("boxed_double_value")
    Double getBoxedDoubleValue();

    @FieldName("float_value")
    float getFloatValue();

    @FieldName("boxed_float_value")
    Float getBoxedFloatValue();

    @FieldName("boolean_value")
    boolean getBooleanValue();

    @FieldName("boxed_boolean_value")
    Boolean getBoxedBooleanValue();

    @FieldName("date")
    Date getDate();

    @FieldName("date")
    Calendar getCalendar();

    @DatePattern("dd/MM/yyyy hh:mm:ss ZZZZ")
    @FieldName("date_test")
    Calendar getCalendarPattern();

    @UnixTimeStamp(inMilliSeconds = false)
    @FieldName("date_test_seconds")
    Calendar getCalendarUnixSeconds();

    @UnixTimeStamp
    @FieldName("date_test_milli_seconds")
    Calendar getCalendarUnixMilliSeconds();

    @Optional
    @FieldName("string_value")
    String getStringValue();

    @FieldName("status")
    Status getStatus();

    @FieldName("status_list")
    List<Status> getStatuses();

    @FieldName("status_set")
    Set<Status> getStatusesSet();

    @Optional
    @FieldName("other")
    TestEntity getOther();

    @FieldName("others")
    List<TestEntity> getOthers();
}
