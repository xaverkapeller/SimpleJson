package com.github.wrdlbrnft.simplejson.builder.parser;

import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.simplejson.SimpleJsonAnnotations;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.ParserBuilder;
import com.github.wrdlbrnft.simplejson.builder.implementation.MethodPairInfo;
import com.github.wrdlbrnft.simplejson.models.MappedValue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Created by kapeller on 24/04/15.
 */
class ElementParserResolver {

    private final Map<String, Field> mParserMap = new HashMap<>();
    private final List<Field> mFields = new ArrayList<>();

    private final ProcessingEnvironment mProcessingEnvironment;
    private final ParserBuilder.BuildCache mBuildCache;
    private final TypeElement mInterfaceType;

    public ElementParserResolver(ProcessingEnvironment processingEnvironment, TypeElement interfaceType, ParserBuilder.BuildCache buildCache) {
        mProcessingEnvironment = processingEnvironment;
        mInterfaceType = interfaceType;
        mBuildCache = buildCache;
    }

    public Field getElementParserField(MappedValue mappedValue) {
        final TypeMirror type = mappedValue.getItemType();

        final TypeElement element = (TypeElement) mProcessingEnvironment.getTypeUtils().asElement(type);
        final String key = type.toString();
        if (mParserMap.containsKey(key)) {
            return mParserMap.get(key);
        }

        final Field field;
        if (Utils.isSubTypeOf(mProcessingEnvironment, type, Enum.class) && Utils.hasAnnotation(element, SimpleJsonAnnotations.JSON_ENUM)) {
            field = createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.of(element)),
                    mBuildCache.getEnumParser(element)
            );
        } else if (Utils.isSameType(type, String.class)) {
            field = createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.STRING),
                    SimpleJsonTypes.STRING_PARSER
            );
        } else if (Utils.isSameType(type, int.class) || Utils.isSameType(type, Integer.class)) {
            field = createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.Boxed.INTEGER),
                    SimpleJsonTypes.INTEGER_PARSER
            );
        } else if (Utils.isSameType(type, double.class) || Utils.isSameType(type, Double.class)) {
            field = createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.Boxed.DOUBLE),
                    SimpleJsonTypes.DOUBLE_PARSER
            );
        } else if (Utils.isSameType(type, long.class) || Utils.isSameType(type, Long.class)) {
            field = createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.Boxed.LONG),
                    SimpleJsonTypes.LONG_PARSER
            );
        } else if (Utils.isSameType(type, boolean.class) || Utils.isSameType(type, Boolean.class)) {
            field = createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.Boxed.BOOLEAN),
                    SimpleJsonTypes.BOOLEAN_PARSER
            );
        } else if (Utils.isSameType(type, Date.class)) {
            field = createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.DATE),
                    SimpleJsonTypes.DATE_PARSER
            );
        } else if (Utils.isSameType(type, Calendar.class)) {
            field = createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.CALENDAR),
                    SimpleJsonTypes.CALENDAR_PARSER
            );
        } else if (Utils.hasAnnotation(element, SimpleJsonAnnotations.JSON_ENTITY)) {
            field = createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.of(type)),
                    mBuildCache.getEntityParser(element)
            );
        } else {
            final MethodPairInfo methodPairInfo = mappedValue.getMethodPairInfo();
            final AnnotationValue parserClassValue = methodPairInfo.findAnnotationValue(SimpleJsonAnnotations.FIELD_NAME, "parserClass");
            if (parserClassValue != null) {
                final TypeMirror parserTypeMirror = (TypeMirror) parserClassValue.getValue();
                field = createElementParserField(
                        Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.of(type)),
                        Types.of(parserTypeMirror)
                );
            } else {

                final Type parser = mBuildCache.getCustomParser(element);
                if (parser == null) {
                    mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not find a parser for " + element.getSimpleName() + "!!1 Have you forgot to annotate it? If the class is a framework class then most likely it is not supported to be used in entities created with this library.", mInterfaceType);
                    return null;
                }

                field = createElementParserField(
                        Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.of(type)),
                        parser
                );
            }
        }

        mParserMap.put(key, field);
        mFields.add(field);

        return field;
    }

    private Field createElementParserField(Type baseType, Type implType) {
        return new Field.Builder()
                .setType(baseType)
                .setModifiers(EnumSet.of(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC))
                .setInitialValue(implType.newInstance())
                .build();
    }

    public List<Field> getFields() {
        return mFields;
    }
}
