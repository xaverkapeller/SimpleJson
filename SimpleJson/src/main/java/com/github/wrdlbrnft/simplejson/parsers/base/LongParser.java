package com.github.wrdlbrnft.simplejson.parsers.base;

import com.github.wrdlbrnft.simplejson.exceptions.SimpleJsonException;
import com.github.wrdlbrnft.simplejson.parsers.ElementParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kapeller on 21/04/15.
 */
public class LongParser implements ElementParser<Long> {

    @Override
    public Long fromJsonObject(JSONObject object, String key) throws SimpleJsonException {
        try {
            return object.getLong(key);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to get Long value with key \"" + key + "\" from json: " + object, e);
        }
    }

    @Override
    public void toJsonObject(JSONObject object, String key, Long value) throws SimpleJsonException {
        try {
            object.put(key, value);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to add Long value " + value + " with key \"" + key + "\" to json: " + object, e);
        }
    }

    @Override
    public Long fromJsonArray(JSONArray array, int index) throws SimpleJsonException {
        try {
            return array.getLong(index);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to get Long value with index " + index + " from json array: " + array, e);
        }
    }

    @Override
    public void toJsonArray(JSONArray array, Long value) throws SimpleJsonException {
        array.put(value);
    }
}
