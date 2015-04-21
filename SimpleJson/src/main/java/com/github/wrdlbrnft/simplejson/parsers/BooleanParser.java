package com.github.wrdlbrnft.simplejson.parsers;

import com.github.wrdlbrnft.simplejson.ElementParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kapeller on 21/04/15.
 */
public class BooleanParser implements ElementParser<Boolean> {

    @Override
    public Boolean fromJsonObject(JSONObject object, String key) throws JSONException {
        return object.getBoolean(key);
    }

    @Override
    public void toJsonObject(JSONObject object, String key, Boolean value) throws JSONException {
        object.put(key, value);
    }

    @Override
    public Boolean fromJsonArray(JSONArray array, int index) throws JSONException {
        return array.getBoolean(index);
    }

    @Override
    public void toJsonArray(JSONArray array, Boolean value) throws JSONException {
        array.put(value);
    }
}
