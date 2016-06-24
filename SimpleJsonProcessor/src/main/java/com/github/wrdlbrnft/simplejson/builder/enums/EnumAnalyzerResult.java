package com.github.wrdlbrnft.simplejson.builder.enums;

import com.github.wrdlbrnft.codebuilder.elements.values.Value;

import java.util.Map;

import javax.lang.model.element.Element;

/**
 * Created by kapeller on 09/07/15.
 */
class EnumAnalyzerResult {
    private final Map<Value, Element> mValueElementMap;
    private final Map<Element, Value> mElementValueMap;
    private final Element mDefaultElement;

    public EnumAnalyzerResult(Map<Value, Element> valueElementMap, Map<Element, Value> elementValueMap, Element defaultElement) {
        mValueElementMap = valueElementMap;
        mElementValueMap = elementValueMap;
        mDefaultElement = defaultElement;
    }

    public Map<Value, Element> getValueElementMap() {
        return mValueElementMap;
    }

    public Map<Element, Value> getElementValueMap() {
        return mElementValueMap;
    }

    public Element getDefaultElement() {
        return mDefaultElement;
    }
}
