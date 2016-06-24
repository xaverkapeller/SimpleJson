package com.github.wrdlbrnft.simplejson.builder.enums;

import com.github.wrdlbrnft.codebuilder.elements.values.Value;
import com.github.wrdlbrnft.codebuilder.elements.values.Values;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.simplejson.SimpleJsonAnnotations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Created by kapeller on 09/07/15.
 */
class EnumAnalyzer {

    private Map<Element, Value> mElementValueMap;
    private Map<Value, Element> mValueElementMap;
    private Set<Object> mValues;

    private final ProcessingEnvironment mProcessingEnvironment;

    private Element mDefaultElement;

    EnumAnalyzer(ProcessingEnvironment processingEnvironment) {
        mProcessingEnvironment = processingEnvironment;
    }

    public EnumAnalyzerResult analyze(TypeElement enumElement) {
        reset();

        for (Element element : enumElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.ENUM_CONSTANT) {
                final AnnotationInfo info = getAnnotationInfo(element);
                if (validate(element, info)) {
                    handleEnumValue(element, info);
                }
            }
        }

        return new EnumAnalyzerResult(mValueElementMap, mElementValueMap, mDefaultElement);
    }

    private void reset() {
        mElementValueMap = new HashMap<>();
        mValueElementMap = new HashMap<>();
        mValues = new HashSet<>();
        mDefaultElement = null;
    }

    private void handleEnumValue(Element element, AnnotationInfo info) {

        final Value value = getMappedValue(element, info);
        if (value != null) {
            if (mValues.contains(value)) {
                mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "You are using the same value to map multiple fields in the Enum! Each field needs to have a unique mapped value!!1", element);
            } else {
                mValues.add(value);
            }

            mElementValueMap.put(element, value);
            mValueElementMap.put(value, element);
        }

        if (info.mHasMapDefault) {
            if (mDefaultElement == null) {
                mDefaultElement = element;
            } else {
                mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "You can only annotate one value in the enum with @MapDefault!!1", mDefaultElement);
                mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "You can only annotate one value in the enum with @MapDefault!!1", element);
            }
        }
    }

    private Value getMappedValue(Element element, AnnotationInfo info) {
        if (info.mHasMapTo) {
            final AnnotationValue annotationValue = Utils.getAnnotationValue(element, SimpleJsonAnnotations.MAP_TO, "value");
            return Values.of((String) annotationValue.getValue());
        }

        return null;
    }

    private boolean validate(Element element, AnnotationInfo info) {
        if (!info.mHasMapDefault && !info.mHasMapTo) {
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, element.getSimpleName() + " is missing an @Map annotation! Add @MapTo or @MapDefault to specify how to map the values.", element);
            return false;
        }

        return true;
    }

    private AnnotationInfo getAnnotationInfo(Element element) {
        boolean hasMapTo = Utils.hasAnnotation(element, SimpleJsonAnnotations.MAP_TO);
        boolean hasMapDefault = Utils.hasAnnotation(element, SimpleJsonAnnotations.MAP_DEFAULT);
        return new AnnotationInfo(hasMapTo, hasMapDefault);
    }

    private static class AnnotationInfo {
        private final boolean mHasMapTo;
        private final boolean mHasMapDefault;

        private AnnotationInfo(boolean hasMapTo, boolean hasMapDefault) {
            mHasMapTo = hasMapTo;
            mHasMapDefault = hasMapDefault;
        }
    }
}
