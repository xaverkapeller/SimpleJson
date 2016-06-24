package com.github.wrdlbrnft.simplejson.parsers.extensions;

import com.github.wrdlbrnft.simplejson.parsers.ParserExtension;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by kapeller on 21/06/16.
 */

public class CalendarParser extends ParserExtension<Date, Calendar> {

    public CalendarParser() {
        super(new DateParser());
    }

    @Override
    protected Calendar convertUp(Date input) {
        if (input == null) {
            return null;
        }

        final Calendar instance = Calendar.getInstance();
        instance.setTime(input);
        return instance;
    }

    @Override
    protected Date convertDown(Calendar input) {
        if (input == null) {
            return null;
        }

        return input.getTime();
    }
}
