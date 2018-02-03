package com.github.wrdlbrnft.simplejson.parsers.base.date;

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

public class UnixTimeStampDateFormatter implements DateFormatter {

    private final boolean mInMilliSeconds;

    public UnixTimeStampDateFormatter(boolean inMilliSeconds) {
        mInMilliSeconds = inMilliSeconds;
    }

    public UnixTimeStampDateFormatter() {
        this(true);
    }

    @Override
    public Date fromJsonObject(JSONObject object, String key) throws JSONException, ParseException {
        final long timeStamp = mInMilliSeconds ? object.getLong(key) : object.getLong(key) * 1000L;
        return new Date(timeStamp);
    }

    @Override
    public void toJsonObject(JSONObject object, String key, Date value) throws JSONException {
        final long timeStamp = mInMilliSeconds ? value.getTime() : value.getTime() / 1000L;
        object.put(key, timeStamp);
    }

    @Override
    public Date fromJsonArray(JSONArray array, int index) throws JSONException, ParseException {
        final long timeStamp = mInMilliSeconds ? array.getLong(index) : array.getLong(index) * 1000L;
        return new Date(timeStamp);
    }

    @Override
    public void toJsonArray(JSONArray array, Date value) {
        final long timeStamp = mInMilliSeconds ? value.getTime() : value.getTime() / 1000L;
        array.put(timeStamp);
    }
}
