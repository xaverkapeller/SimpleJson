package com.github.wrdlbrnft.simplejson.parsers;

import com.github.wrdlbrnft.simplejson.ElementParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kapeller on 21/04/15.
 */
public class LongParser implements ElementParser<Long> {

    @Override
    public Long fromJsonObject(JSONObject object, String key) throws JSONException {
        return object.getLong(key);
    }

    @Override
    public void toJsonObject(JSONObject object, String key, Long value) throws JSONException {
        object.put(key, value);
    }

    @Override
    public Long fromJsonArray(JSONArray array, int index) throws JSONException {
        return array.getLong(index);
    }

    @Override
    public void toJsonArray(JSONArray array, Long value) throws JSONException {
        array.put(value);
    }
}
