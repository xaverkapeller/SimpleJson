package com.github.wrdlbrnft.simplejson.parsers.base.date;

import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 03/02/2018
 */

public class PatternDateFormatter implements DateFormatter {

    private final SimpleDateFormat mDateFormat;

    @SuppressLint("SimpleDateFormat")
    public PatternDateFormatter(String pattern) {
        mDateFormat = new SimpleDateFormat(pattern);
    }

    @Override
    public Date fromJsonObject(JSONObject object, String key) throws JSONException, ParseException {
        final String dateString = object.getString(key);
        return mDateFormat.parse(dateString);
    }

    @Override
    public void toJsonObject(JSONObject object, String key, Date value) throws JSONException {
        object.put(key, mDateFormat.format(value));
    }

    @Override
    public Date fromJsonArray(JSONArray array, int index) throws JSONException, ParseException {
        final String dateString = array.getString(index);
        return mDateFormat.parse(dateString);
    }

    @Override
    public void toJsonArray(JSONArray array, Date value) {
        array.put(mDateFormat.format(value));
    }
}
