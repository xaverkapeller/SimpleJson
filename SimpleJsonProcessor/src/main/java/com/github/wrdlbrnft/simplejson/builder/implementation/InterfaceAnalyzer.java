package com.github.wrdlbrnft.simplejson.builder.implementation;

import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.simplejson.SimpleJsonAnnotations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Created by kapeller on 08/07/15.
 */
class InterfaceAnalyzer {

    private final Map<String, MethodPairInfo> mPairInfoMap = new LinkedHashMap<>();
    private final ProcessingEnvironment mProcessingEnvironment;

    private TypeElement mCurrentElement;

    public InterfaceAnalyzer(ProcessingEnvironment processingEnvironment) {
        mProcessingEnvironment = processingEnvironment;
    }

    public List<MethodPairInfo> analyze(TypeElement model) {
        mCurrentElement = model;
        mPairInfoMap.clear();

        final List<? extends Element> members = Utils.getAllDeclaredInterfaceElements(mProcessingEnvironment, model);
        for (Element element : members) {
            if (element.getKind() == ElementKind.METHOD) {
                final ExecutableElement method = (ExecutableElement) element;
                final String name = method.getSimpleName().toString();
                if ("set".equals(name.substring(0, 3))) {
                    handleSet(method);
                } else if ("get".equals(name.substring(0, 3))) {
                    handleGet(method);
                } else if ("is".equals(name.substring(0, 2)) && Character.isUpperCase(name.charAt(2))) {
                    handleIs(method);
                } else {
                    mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "The method " + name + " of " + model.getSimpleName() + " is not a valid getter or setter!", method);
                }
            }
        }

        checkValidityOfMethodPairs();
        return new ArrayList<>(mPairInfoMap.values());
    }

    private void handleIs(ExecutableElement method) {
        final String name = method.getSimpleName().toString();
        final String groupName = name.substring(2, name.length());
        registerGetter(groupName, method);
        recordFieldName(groupName, method);
    }

    private void handleGet(ExecutableElement method) {
        final String name = method.getSimpleName().toString();
        final String groupName = name.substring(3, name.length());
        registerGetter(groupName, method);
        recordFieldName(groupName, method);
    }

    private void handleSet(ExecutableElement method) {
        final String name = method.getSimpleName().toString();
        final String key = name.substring(3, name.length());
        registerSetter(key, method);

        if (Utils.hasAnnotation(method, SimpleJsonAnnotations.FIELD_NAME)) {
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "The method " + method.getSimpleName() + " of " + mCurrentElement.getSimpleName() + " is a valid setter and therefore must not have an @FieldName annotation! Annotate the getter methods instead!", method);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void recordFieldName(String groupName, ExecutableElement method) {
        if (Utils.hasAnnotation(method, SimpleJsonAnnotations.FIELD_NAME)) {
            final String fieldName = (String) Utils.getAnnotationValue(method, SimpleJsonAnnotations.FIELD_NAME, "value").getValue();
            registerFieldName(groupName, fieldName);
        } else {
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "The method " + method.getSimpleName() + " of " + mCurrentElement.getSimpleName() + " is a valid getter but it is missing the required @FieldName annotation!", method);
        }
    }

    private void checkValidityOfMethodPairs() {
        for (MethodPairInfo info : mPairInfoMap.values()) {
            validateGetter(info);
            validateSetter(info);
        }
    }

    private void validateGetter(MethodPairInfo info) {
        final ExecutableElement setter = info.getSetter();
        final ExecutableElement getter = info.getGetter();
        if (getter == null) {
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "A getter method associated with " + setter.getSimpleName() + " is missing from " + mCurrentElement.getSimpleName(), setter);
            return;
        }

        final List<? extends VariableElement> getterParameters = getter.getParameters();
        final TypeMirror getterReturnType = getter.getReturnType();

        if (info.getFieldName() == null) {
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "@FieldName annotation is missing from " + getter.getSimpleName() + " in " + mCurrentElement.getSimpleName() + "!", getter);
        }

        if (Utils.isSameType(getterReturnType, void.class)) {
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "The method " + getter.getSimpleName() + " of " + mCurrentElement.getSimpleName() + " is a getter method but it does not return any value! Getter methods must not have a void return type!", getter);
        }

        if (!getterParameters.isEmpty()) {
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "The method " + getter.getSimpleName() + " of " + mCurrentElement.getSimpleName() + " is a getter method and therefore must not have any parameters!", getter);
        }
    }

    private void validateSetter(MethodPairInfo info) {
        final ExecutableElement setter = info.getSetter();
        final ExecutableElement getter = info.getGetter();
        if (setter != null) {
            final List<? extends VariableElement> setterParameters = setter.getParameters();
            final TypeMirror setterReturnType = setter.getReturnType();
            final TypeMirror getterReturnType = getter.getReturnType();

            if (!Utils.isSameType(setterReturnType, void.class)) {
                mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "The method " + setter.getSimpleName() + " of " + mCurrentElement.getSimpleName() + " is a setter method but it returns a value! Setter methods are required to have a void return type!", setter);
            }

            if (setterParameters.size() != 1) {
                mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "The method " + setter.getSimpleName() + " of " + mCurrentElement.getSimpleName() + " is a setter method and therefore must have exactly one parameter!", setter);
            }

            if (setterParameters.size() == 1 && !mProcessingEnvironment.getTypeUtils().isSameType(getterReturnType, setterParameters.get(0).asType())) {
                mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "The parameter type of " + setter.getSimpleName() + " and the return type of " + getter.getSimpleName() + " in " + mCurrentElement.getSimpleName() + " do not match!", setter);
            }
        }
    }

    private void registerSetter(String groupName, ExecutableElement method) {
        ensurePairInfoExists(groupName);
        final MethodPairInfo info = mPairInfoMap.get(groupName);
        final ExecutableElement setter = info.getSetter();
        if (setter != null) {
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "There exists multiple setters in " + mCurrentElement.getSimpleName() + " which have similar names. Similar naming implies that those methods operate on the same field. For example an interface with the methods 'getValue' and 'isValue' at the same time makes no sense and is rejected.", setter);
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "There exists multiple setters in " + mCurrentElement.getSimpleName() + " which have similar names. Similar naming implies that those methods operate on the same field. For example an interface with the methods 'getValue' and 'isValue' at the same time makes no sense and is rejected.", method);
        }

        info.setSetter(method);
    }

    private void registerGetter(String groupName, ExecutableElement method) {
        ensurePairInfoExists(groupName);
        final MethodPairInfo info = mPairInfoMap.get(groupName);
        final ExecutableElement getter = info.getGetter();
        if (getter != null) {
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "There exists multiple getters in " + mCurrentElement.getSimpleName() + " which have similar names. Similar naming implies that those methods operate on the same field. For example an interface with the methods 'getValue' and 'isValue' at the same time makes no sense and is rejected.", getter);
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "There exists multiple getters in " + mCurrentElement.getSimpleName() + " which have similar names. Similar naming implies that those methods operate on the same field. For example an interface with the methods 'getValue' and 'isValue' at the same time makes no sense and is rejected.", method);
        }

        info.setGetter(method);
    }

    private void registerFieldName(String groupName, String fieldName) {
        ensurePairInfoExists(groupName);
        final MethodPairInfo info = mPairInfoMap.get(groupName);
        if (info.getFieldName() != null) {
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "A fieldName for the " + groupName + " methods has already been recorded? Check your spelling! Do you have multiple @FieldName annotations or multiple getters with similar names?", info.getGetter());
        }

        info.setFieldName(fieldName);
    }

    private void ensurePairInfoExists(String groupName) {
        if (!mPairInfoMap.containsKey(groupName)) {
            final MethodPairInfo info = new MethodPairInfo();
            info.setGroupName(groupName);
            mPairInfoMap.put(groupName, info);
        }
    }
}
