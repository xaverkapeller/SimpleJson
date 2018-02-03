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
public interface DateFormatter {
    Date fromJsonObject(JSONObject object, String key) throws JSONException, ParseException;
    void toJsonObject(JSONObject object, String key, Date value) throws JSONException;
    Date fromJsonArray(JSONArray array, int index) throws JSONException, ParseException;
    void toJsonArray(JSONArray array, Date value);
}
