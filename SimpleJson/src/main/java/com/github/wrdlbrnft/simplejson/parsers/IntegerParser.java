package com.github.wrdlbrnft.simplejson.parsers;

import com.github.wrdlbrnft.simplejson.ElementParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kapeller on 21/04/15.
 */
public class IntegerParser implements ElementParser<Integer> {

    @Override
    public Integer fromJsonObject(JSONObject object, String key) throws JSONException {
        return object.getInt(key);
    }

    @Override
    public void toJsonObject(JSONObject object, String key, Integer value) throws JSONException {
        object.put(key, value);
    }

    @Override
    public Integer fromJsonArray(JSONArray array, int index) throws JSONException {
        return array.getInt(index);
    }

    @Override
    public void toJsonArray(JSONArray array, Integer value) throws JSONException {
        array.put(value);
    }
}
