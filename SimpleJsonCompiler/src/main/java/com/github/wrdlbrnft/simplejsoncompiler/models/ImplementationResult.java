package com.github.wrdlbrnft.simplejsoncompiler.models;

import com.github.wrdlbrnft.codebuilder.elements.Type;

import java.util.List;

/**
* Created by kapeller on 21/04/15.
*/
public class ImplementationResult {

    private final Type mImplType;
    private final List<MappedValue> mMappedValues;

    public ImplementationResult(Type implType, List<MappedValue> mappedValues) {
        mImplType = implType;
        mMappedValues = mappedValues;
    }

    public Type getImplType() {
        return mImplType;
    }

    public List<MappedValue> getMappedValues() {
        return mMappedValues;
    }
}
