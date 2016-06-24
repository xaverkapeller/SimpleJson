package com.github.wrdlbrnft.simplejson.parsers;

import com.github.wrdlbrnft.simplejson.exceptions.SimpleJsonException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
* Created by kapeller on 21/04/15.
*/
public interface ElementParser<T> {
    T fromJsonObject(JSONObject object, String key) throws SimpleJsonException;
    void toJsonObject(JSONObject object, String key, T value) throws SimpleJsonException;
    T fromJsonArray(JSONArray array, int index) throws SimpleJsonException;
    void toJsonArray(JSONArray array, T value) throws SimpleJsonException;
}
