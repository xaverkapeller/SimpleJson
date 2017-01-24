package com.github.wrdlbrnft.simplejson.parsers;

import com.github.wrdlbrnft.simplejson.exceptions.SimpleJsonException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kapeller on 21/06/16.
 */
public abstract class BaseEnumParser<T extends Enum<T>> implements EnumParser<T> {

    public final T fromJsonObject(JSONObject object, String key) throws SimpleJsonException {
        try {
            return parse(object.getString(key));
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to get Enum value with key \"" + key + "\" from json: " + object, e);
        }
    }

    public final void toJsonObject(JSONObject object, String key, T value) throws SimpleJsonException {
        try {
            object.put(key, format(value));
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to add Enum value " + value + " with the key \"" + key + "\" to json: " + object, e);
        }
    }

    public final T fromJsonArray(JSONArray array, int index) throws SimpleJsonException {
        try {
            return parse(array.getString(index));
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to get Enum value with index " + index + " from json array: " + array, e);
        }
    }

    public final void toJsonArray(JSONArray array, T value) throws SimpleJsonException {
        array.put(format(value));
    }
}
