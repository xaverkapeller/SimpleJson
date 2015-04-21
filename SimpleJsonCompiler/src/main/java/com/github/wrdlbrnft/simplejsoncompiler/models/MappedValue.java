package com.github.wrdlbrnft.simplejsoncompiler.models;

import com.github.wrdlbrnft.codebuilder.elements.Type;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
* Created by kapeller on 21/04/15.
*/
public class MappedValue {
    public enum ValueType {
        VALUE,
        LIST,
        SET
    }

    private final boolean mOptional;
    private final Type mType;
    private final String mKey;
    private final ValueType mValueType;
    private final ExecutableElement mMethod;

    public MappedValue(String key, Type type, boolean optional, ValueType valueType, ExecutableElement method) {
        mType = type;
        mKey = key;
        mOptional = optional;
        mValueType = valueType;
        mMethod = method;
    }

    public boolean isOptional() {
        return mOptional;
    }

    public Type getType() {
        return mType;
    }

    public String getKey() {
        return mKey;
    }

    public ValueType getValueType() {
        return mValueType;
    }

    public Element getMethod() {
        return mMethod;
    }
}
