package com.github.wrdlbrnft.simplejson.builder.implementation;

import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.simplejson.builder.implementation.MethodPairInfo;

import javax.lang.model.type.TypeMirror;

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
    private final TypeMirror mBaseType;
    private final TypeMirror mItemType;
    private final String mFieldName;
    private final ValueType mValueType;
    private final boolean mParentReference;
    private final MethodPairInfo mMethodPairInfo;
    private final Field mField;

    public MappedValue(String fieldName, TypeMirror baseType, TypeMirror itemType, boolean optional, ValueType valueType, boolean parentReference, MethodPairInfo methodPairInfo, Field field) {
        mBaseType = baseType;
        mItemType = itemType;
        mFieldName = fieldName;
        mOptional = optional;
        mValueType = valueType;
        mParentReference = parentReference;
        mMethodPairInfo = methodPairInfo;
        mField = field;

    }

    public boolean isOptional() {
        return mOptional;
    }

    public TypeMirror getItemType() {
        return mItemType;
    }

    public String getFieldName() {
        return mFieldName;
    }

    public ValueType getValueType() {
        return mValueType;
    }

    public TypeMirror getBaseType() {
        return mBaseType;
    }

    public boolean isParentReference() {
        return mParentReference;
    }

    public MethodPairInfo getMethodPairInfo() {
        return mMethodPairInfo;
    }

    public Field getField() {
        return mField;
    }
}
