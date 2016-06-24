package com.github.wrdlbrnft.simplejson.parsers;

import com.github.wrdlbrnft.simplejson.exceptions.SimpleJsonException;

/**
 * Created by kapeller on 21/06/16.
 */

public interface EnumParser<T extends Enum<T>> extends ElementParser<T> {
    String format(T value) throws SimpleJsonException;
    T parse(String value) throws SimpleJsonException;
}
