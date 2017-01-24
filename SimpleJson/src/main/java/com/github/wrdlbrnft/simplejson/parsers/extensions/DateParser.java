package com.github.wrdlbrnft.simplejson.parsers.extensions;

import com.github.wrdlbrnft.simplejson.parsers.ParserExtension;
import com.github.wrdlbrnft.simplejson.parsers.base.LongParser;

import java.util.Date;

/**
 * Created by kapeller on 30/09/15.
 */
public class DateParser extends ParserExtension<Long, Date> {

    public DateParser() {
        super(new LongParser());
    }

    @Override
    protected Date convertUp(Long input) {
        return new Date(input);
    }

    @Override
    protected Long convertDown(Date input) {
        if (input == null) {
            return null;
        }

        return input.getTime();
    }
}
