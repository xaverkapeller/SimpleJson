package com.github.wrdlbrnft.simplejson;

import org.json.JSONException;

import java.util.List;

/**
 * Created by kapeller on 21/04/15.
 */
public interface Parser<T> {
    public T fromJson(String json) throws JSONException;
    public List<T> fromJsonArray(String json) throws JSONException;
    public String toJson(T entity) throws JSONException;
    public String toJson(List<T> entities) throws JSONException;
}
