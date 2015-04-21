package com.github.wrdlbrnft.simplejson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
* Created by kapeller on 21/04/15.
*/
public interface ElementParser<T> {
    public T fromJsonObject(JSONObject object, String key) throws JSONException;
    public void toJsonObject(JSONObject object, String key, T value) throws JSONException;
    public T fromJsonArray(JSONArray array, int index) throws JSONException;
    public void toJsonArray(JSONArray array, T value) throws JSONException;
}
