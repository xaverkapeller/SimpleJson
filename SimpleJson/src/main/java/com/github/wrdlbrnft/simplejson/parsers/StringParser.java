package com.github.wrdlbrnft.simplejson.parsers;

import com.github.wrdlbrnft.simplejson.ElementParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kapeller on 21/04/15.
 */
public class StringParser implements ElementParser<String> {

    @Override
    public String fromJsonObject(JSONObject object, String key) throws JSONException {
        return object.getString(key);
    }

    @Override
    public void toJsonObject(JSONObject object, String key, String value) throws JSONException {
        object.put(key, value);
    }

    @Override
    public String fromJsonArray(JSONArray array, int index) throws JSONException {
        return array.getString(index);
    }

    @Override
    public void toJsonArray(JSONArray array, String value) throws JSONException {
        array.put(value);
    }
}
