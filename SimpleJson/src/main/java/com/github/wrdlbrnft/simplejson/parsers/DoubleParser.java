package com.github.wrdlbrnft.simplejson.parsers;

import com.github.wrdlbrnft.simplejson.ElementParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kapeller on 21/04/15.
 */
public class DoubleParser implements ElementParser<Double> {

    @Override
    public Double fromJsonObject(JSONObject object, String key) throws JSONException {
        return object.getDouble(key);
    }

    @Override
    public void toJsonObject(JSONObject object, String key, Double value) throws JSONException {
        object.put(key, value);
    }

    @Override
    public Double fromJsonArray(JSONArray array, int index) throws JSONException {
        return array.getDouble(index);
    }

    @Override
    public void toJsonArray(JSONArray array, Double value) throws JSONException {
        array.put(value);
    }
}
