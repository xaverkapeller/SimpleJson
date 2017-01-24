package com.github.wrdlbrnft.simplejson.parsers;

import com.github.wrdlbrnft.simplejson.exceptions.SimpleJsonException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by kapeller on 13/01/16.
 */
public abstract class BaseParser<T> implements Parser<T> {

    @Override
    public final T fromJsonObject(JSONObject object, String key) throws SimpleJsonException {
        try {
            final JSONObject entityObject = object.getJSONObject(key);
            return fromJsonObject(entityObject);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to get object with key \"" + key + "\" from json: " + object, e);
        }
    }

    @Override
    public final void toJsonObject(JSONObject object, String key, T value) throws SimpleJsonException {
        try {
            object.put(key, toJsonObject(value));
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to add object " + value + " with the key \"" + key + "\" to this json: " + object, e);
        }
    }

    @Override
    public final T fromJsonArray(JSONArray array, int index) throws SimpleJsonException {
        try {
            final JSONObject entityObject = array.getJSONObject(index);
            return fromJsonObject(entityObject);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to get object with index " + index + " from json array: " + array, e);
        }
    }

    @Override
    public final void toJsonArray(JSONArray array, T value) throws SimpleJsonException {
        array.put(toJsonObject(value));
    }

    @Override
    public final T fromJson(String json) throws SimpleJsonException {
        try {
            final JSONObject object = new JSONObject(json);
            return fromJsonObject(object);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to parse object from json: " + json, e);
        }
    }

    @Override
    public final List<T> fromJsonArray(String json) throws SimpleJsonException {
        try {
            final List<T> list = new ArrayList<>();
            final JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                final JSONObject object = array.getJSONObject(i);
                list.add(fromJsonObject(object));
            }
            return list;
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to parse list of objects from json: " + json, e);
        }
    }

    @Override
    public final String toJson(T entity) throws SimpleJsonException {
        final JSONObject object = toJsonObject(entity);
        return object.toString();
    }

    @Override
    public final String toJson(Collection<T> entities) throws SimpleJsonException {
        final JSONArray array = new JSONArray();
        for (T entity : entities) {
            final JSONObject t = toJsonObject(entity);
            array.put(t);
        }
        return array.toString();
    }

    @Override
    public final T fromJsonObject(JSONObject object) throws SimpleJsonException {
        try {
            return convertFromJson(object);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to parse object from json: " + object, e);
        }
    }

    @Override
    public final JSONObject toJsonObject(T entity) throws SimpleJsonException {
        try {
            return convertToJson(entity);
        } catch (JSONException e) {
            throw new SimpleJsonException("Failed to format object to json: " + entity, e);
        }
    }

    protected abstract T convertFromJson(JSONObject object) throws JSONException;
    protected abstract JSONObject convertToJson(T entity) throws JSONException;
}
