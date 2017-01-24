package com.github.wrdlbrnft.simplejson.parsers.base;

import com.github.wrdlbrnft.simplejson.exceptions.SimpleJsonException;
import com.github.wrdlbrnft.simplejson.parsers.ElementParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kapeller on 21/04/15.
 */
public class StringParser implements ElementParser<String> {

    @Override
    public String fromJsonObject(JSONObject object, String key) throws SimpleJsonException {
        try {
            return object.getString(key);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to get String value with key \"" + key + "\" from json: " + object, e);
        }
    }

    @Override
    public void toJsonObject(JSONObject object, String key, String value) throws SimpleJsonException {
        try {
            object.put(key, value);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to add String value \"" + value + "\" with key \"" + key + "\" to json: " + object, e);
        }
    }

    @Override
    public String fromJsonArray(JSONArray array, int index) throws SimpleJsonException {
        try {
            return array.getString(index);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to get String value with index " + index + " from json array: " + array, e);
        }
    }

    @Override
    public void toJsonArray(JSONArray array, String value) throws SimpleJsonException {
        array.put(value);
    }
}
