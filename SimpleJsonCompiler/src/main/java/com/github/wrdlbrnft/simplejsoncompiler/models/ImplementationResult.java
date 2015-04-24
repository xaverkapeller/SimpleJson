package com.github.wrdlbrnft.simplejsoncompiler.models;

import com.github.wrdlbrnft.codebuilder.elements.Type;
import com.github.wrdlbrnft.codebuilder.elements.Variable;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by kapeller on 21/04/15.
 */
public class ImplementationResult {

    private final Type mInterfaceType;
    private final Type mImplType;
    private final List<MappedValue> mMappedValues;

    public ImplementationResult(Type implType, Type interfaceType, List<MappedValue> mappedValues) {
        mInterfaceType = interfaceType;
        mImplType = implType;
        mMappedValues = mappedValues;
    }

    public Type getImplType() {
        return mImplType;
    }

    public List<MappedValue> getMappedValues() {
        return mMappedValues;
    }

    public Type getInterfaceType() {
        return mInterfaceType;
    }
}
