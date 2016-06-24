package com.github.wrdlbrnft.simplejson.parsers.base;

import com.github.wrdlbrnft.simplejson.exceptions.SimpleJsonException;
import com.github.wrdlbrnft.simplejson.parsers.ElementParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kapeller on 21/04/15.
 */
public class DoubleParser implements ElementParser<Double> {

    @Override
    public Double fromJsonObject(JSONObject object, String key) throws SimpleJsonException {
        try {
            return object.getDouble(key);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to get Double value with key \"" + key + "\" from json: " + object, e);
        }
    }

    @Override
    public void toJsonObject(JSONObject object, String key, Double value) throws SimpleJsonException {
        try {
            object.put(key, value);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to add Double value " + value + " with key \"" + key + "\" to json: " + object, e);
        }
    }

    @Override
    public Double fromJsonArray(JSONArray array, int index) throws SimpleJsonException {
        try {
            return array.getDouble(index);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to get Double value with index " + index + " from json array: " + array, e);
        }
    }

    @Override
    public void toJsonArray(JSONArray array, Double value) throws SimpleJsonException {
        array.put(value);
    }
}
