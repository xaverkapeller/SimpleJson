package com.github.wrdlbrnft.simplejson.parsers;

import com.github.wrdlbrnft.simplejson.ElementParser;
import com.github.wrdlbrnft.simplejson.Parser;
import com.github.wrdlbrnft.simplejson.SimpleJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kapeller on 21/04/15.
 */
public class EntityParser<T> implements ElementParser<T> {

    private final Parser<T> mParser;

    public EntityParser(Class<T> entityClass) {
        mParser = SimpleJson.getParser(entityClass);
    }

    @Override
    public T fromJsonObject(JSONObject object, String key) throws JSONException {
        final JSONObject entityObject = object.getJSONObject(key);
        return mParser.fromJsonObject(entityObject);
    }

    @Override
    public void toJsonObject(JSONObject object, String key, T value) throws JSONException {
        object.put(key, mParser.toJsonObject(value));
    }

    @Override
    public T fromJsonArray(JSONArray array, int index) throws JSONException {
        final JSONObject entityObject = array.getJSONObject(index);
        return mParser.fromJsonObject(entityObject);
    }

    @Override
    public void toJsonArray(JSONArray array, T value) throws JSONException {
        array.put(mParser.toJsonObject(value));
    }
}
