package com.github.wrdlbrnft.simplejson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by kapeller on 21/04/15.
 */
public class SimpleJson {

    public static <T> Parser<T> getParser(Class<T> cls) {
        if (!cls.isInterface()) {
            throw new IllegalStateException("SimpleJson only supports interfaces!");
        }

        final String interfaceName = cls.getName();
        final String parserClassName = interfaceName + "$Parser";
        try {
            final Class<? extends Parser<T>> parserClass = (Class<? extends Parser<T>>) Class.forName(parserClassName);
            return parserClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Could not parse class! Have you set up the SimpleJsonCompiler correctly?", e);
        }
    }

    public static <T> T fromJson(Class<T> cls, String json) throws JSONException {
        final Parser<T> parser = getParser(cls);
        return parser.fromJson(json);
    }

    public static <T> T fromJsonArray(Class<T> cls, String json) throws JSONException {
        final JSONArray array = new JSONArray(json);
        for(int i = 0, count = array.length(); i < count; i++) {
            final JSONObject object = array.getJSONObject(i);
        }
        final Parser<T> parser = getParser(cls);
        return parser.fromJson(json);
    }

    public static <T> String toJson(Class<T> cls, T entity) throws JSONException {
        final Parser<T> parser = getParser(cls);
        return parser.toJson(entity);
    }

    public static <T> String toJson(Class<T> cls, List<T> entities) throws JSONException {
        final Parser<T> parser = getParser(cls);
        return parser.toJson(entities);
    }
}
