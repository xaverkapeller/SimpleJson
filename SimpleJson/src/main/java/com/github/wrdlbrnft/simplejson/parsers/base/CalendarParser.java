package com.github.wrdlbrnft.simplejson.parsers.base;

import com.github.wrdlbrnft.simplejson.parsers.ParserExtension;
import com.github.wrdlbrnft.simplejson.parsers.base.date.DateFormatter;

import java.util.Calendar;
import java.util.Date;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 03/02/2018
 */

public class CalendarParser extends ParserExtension<Date, Calendar> {

    public CalendarParser(DateFormatter dateFormatter) {
        super(new DateParser(dateFormatter));
    }

    @Override
    protected Calendar convertUp(Date input) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(input);
        return calendar;
    }

    @Override
    protected Date convertDown(Calendar input) {
        if (input == null) {
            return null;
        }
        return input.getTime();
    }
}
