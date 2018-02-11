package com.github.wrdlbrnft.simplejson.builder.implementation;

import com.github.wrdlbrnft.codebuilder.implementations.Implementation;

import java.util.List;

import javax.lang.model.element.TypeElement;

/**
 * Created by kapeller on 21/04/15.
 */
public class ImplementationResult {

    private final TypeElement mInterfaceType;
    private final Implementation mImplType;
    private final List<MappedValue> mMappedValues;
    private final ImplementationInfo mImplementationInfo;

    public ImplementationResult(Implementation implType, TypeElement interfaceType, List<MappedValue> mappedValues, ImplementationInfo implementationInfo) {
        mInterfaceType = interfaceType;
        mImplType = implType;
        mMappedValues = mappedValues;
        mImplementationInfo = implementationInfo;
    }

    public Implementation getImplType() {
        return mImplType;
    }

    public List<MappedValue> getMappedValues() {
        return mMappedValues;
    }

    public TypeElement getInterfaceType() {
        return mInterfaceType;
    }

    public ImplementationInfo getImplementationInfo() {
        return mImplementationInfo;
    }
}
