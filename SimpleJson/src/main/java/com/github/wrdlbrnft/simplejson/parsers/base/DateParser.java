package com.github.wrdlbrnft.simplejson.parsers.base;

import com.github.wrdlbrnft.simplejson.exceptions.SimpleJsonException;
import com.github.wrdlbrnft.simplejson.parsers.ElementParser;
import com.github.wrdlbrnft.simplejson.parsers.base.date.DateFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 03/02/2018
 */

public class DateParser implements ElementParser<Date> {

    private final DateFormatter mDateFormatter;

    public DateParser(DateFormatter dateFormatter) {
        mDateFormatter = dateFormatter;
    }

    @Override
    public Date fromJsonObject(JSONObject object, String key) throws SimpleJsonException {
        try {
            return mDateFormatter.fromJsonObject(object, key);
        } catch (JSONException | ParseException e) {
            throw new SimpleJsonException("Failed to get Double value with key \"" + key + "\" from json: " + object, e);
        }
    }

    @Override
    public void toJsonObject(JSONObject object, String key, Date value) throws SimpleJsonException {
        try {
            mDateFormatter.toJsonObject(object, key, value);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to add Double value " + value + " with key \"" + key + "\" to json: " + object, e);
        }
    }

    @Override
    public Date fromJsonArray(JSONArray array, int index) throws SimpleJsonException {
        try {
            return mDateFormatter.fromJsonArray(array, index);
        } catch (JSONException | ParseException e) {
            throw new SimpleJsonException("Failed to get Double value with index " + index + " from json array: " + array, e);
        }
    }

    @Override
    public void toJsonArray(JSONArray array, Date value) throws SimpleJsonException {
        mDateFormatter.toJsonArray(array, value);
    }
}
