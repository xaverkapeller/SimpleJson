package com.github.wrdlbrnft.simplejson.builder.implementation;

import com.github.wrdlbrnft.codebuilder.util.Utils;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

/**
 * Created by kapeller on 08/07/15.
 */
public class MethodPairInfo {
    private String mFieldName;
    private String mGroupName;
    private ExecutableElement mGetter;
    private ExecutableElement mSetter;

    public String getFieldName() {
        return mFieldName;
    }

    public void setFieldName(String fieldName) {
        this.mFieldName = fieldName;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupName(String groupName) {
        this.mGroupName = groupName;
    }

    public ExecutableElement getGetter() {
        return mGetter;
    }

    public void setGetter(ExecutableElement getter) {
        this.mGetter = getter;
    }

    public ExecutableElement getSetter() {
        return mSetter;
    }

    public void setSetter(ExecutableElement setter) {
        this.mSetter = setter;
    }

    public AnnotationValue findAnnotationValue(String annotationClassName, String fieldName) {
        final AnnotationValue getterAnnotationMirror = Utils.getAnnotationValue(mGetter, annotationClassName, fieldName);
        if (getterAnnotationMirror != null) {
            return getterAnnotationMirror;
        }
        return Utils.getAnnotationValue(mGetter, annotationClassName, fieldName);
    }
}
