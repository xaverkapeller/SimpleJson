package com.github.wrdlbrnft.simplejson.parsers;

import com.github.wrdlbrnft.simplejson.exceptions.SimpleJsonException;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by kapeller on 21/06/16.
 */
public abstract class ParserExtension<I, O> implements ElementParser<O> {

    private final ElementParser<I> mBaseParser;

    public ParserExtension(ElementParser<I> baseParser) {
        mBaseParser = baseParser;
    }

    @Override
    public final O fromJsonObject(JSONObject object, String key) throws SimpleJsonException {
        final I input = mBaseParser.fromJsonObject(object, key);
        return convertUp(input);
    }

    @Override
    public final void toJsonObject(JSONObject object, String key, O value) throws SimpleJsonException {
        final I input = convertDown(value);
        mBaseParser.toJsonObject(object, key, input);
    }

    @Override
    public final O fromJsonArray(JSONArray array, int index) throws SimpleJsonException {
        final I input = mBaseParser.fromJsonArray(array, index);
        return convertUp(input);
    }

    @Override
    public final void toJsonArray(JSONArray array, O value) throws SimpleJsonException {
        final I input = convertDown(value);
        mBaseParser.toJsonArray(array, input);
    }

    protected abstract O convertUp(I input);
    protected abstract I convertDown(O input);
}
