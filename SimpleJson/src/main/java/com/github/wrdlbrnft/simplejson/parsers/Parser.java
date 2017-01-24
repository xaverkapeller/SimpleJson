package com.github.wrdlbrnft.simplejson.parsers;

import com.github.wrdlbrnft.simplejson.exceptions.SimpleJsonException;

import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

/**
 * Created by kapeller on 21/04/15.
 */
public interface Parser<T> extends ElementParser<T> {
    T fromJson(String json) throws SimpleJsonException;
    List<T> fromJsonArray(String json) throws SimpleJsonException;
    String toJson(T entity) throws SimpleJsonException;
    String toJson(Collection<T> entities) throws SimpleJsonException;
    T fromJsonObject(JSONObject object) throws SimpleJsonException;
    JSONObject toJsonObject(T entity) throws SimpleJsonException;
}
