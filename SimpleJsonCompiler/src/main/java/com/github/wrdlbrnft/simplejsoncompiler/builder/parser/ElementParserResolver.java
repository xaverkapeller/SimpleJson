package com.github.wrdlbrnft.simplejsoncompiler.builder.parser;

import com.github.wrdlbrnft.codebuilder.elements.Field;
import com.github.wrdlbrnft.codebuilder.elements.Type;
import com.github.wrdlbrnft.codebuilder.impl.ClassBuilder;
import com.github.wrdlbrnft.codebuilder.impl.Types;
import com.github.wrdlbrnft.codebuilder.utils.Utils;
import com.github.wrdlbrnft.simplejsoncompiler.Annotations;
import com.github.wrdlbrnft.simplejsoncompiler.SimpleJsonTypes;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Created by kapeller on 24/04/15.
 */
class ElementParserResolver {

    private final Map<String, Field> mParserMap = new HashMap<>();

    private final ProcessingEnvironment mProcessingEnvironment;
    private final Map<String , Type> mEnumParserMap;
    private final TypeElement mInterfaceType;
    private final ClassBuilder mBuilder;

    public ElementParserResolver(ProcessingEnvironment processingEnvironment, TypeElement interfaceType, ClassBuilder builder, Map<String, Type> enumParserMap) {
        mProcessingEnvironment = processingEnvironment;
        mInterfaceType = interfaceType;
        mBuilder = builder;
        mEnumParserMap = enumParserMap;
    }

    public Field getElementParserField(Type type) {
        final String key = type.fullClassName();
        if (mParserMap.containsKey(key)) {
            return mParserMap.get(key);
        }

        mBuilder.addImport(type);

        final Field field;
        if (type.isSubTypeOf(SimpleJsonTypes.ENUM)) {
            if (mEnumParserMap.containsKey(key)) {
                final Type parserType = mEnumParserMap.get(key);
                field = createElementParserField(parserType);
            } else {
                mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "There is no parser implementation for " + type + "! Have you forgot to annotate it with @JsonEnum?", type.getTypeElement());
                throw new IllegalStateException("There is no parser implementation for " + type + "! Have you forgot to annotate it with @JsonEnum?");
            }
        } else if (type.equals(Types.STRING)) {
            field = createElementParserField(SimpleJsonTypes.STRING_PARSER);
        } else if (type.equals(Types.Primitives.INTEGER)) {
            field = createElementParserField(SimpleJsonTypes.INTEGER_PARSER);
        } else if (type.equals(Types.Primitives.DOUBLE)) {
            field = createElementParserField(SimpleJsonTypes.DOUBLE_PARSER);
        } else if (type.equals(Types.Primitives.LONG)) {
            field = createElementParserField(SimpleJsonTypes.LONG_PARSER);
        } else if (type.equals(Types.Primitives.BOOLEAN)) {
            field = createElementParserField(SimpleJsonTypes.BOOLEAN_PARSER);
        } else if (type.equals(Types.Boxed.INTEGER)) {
            field = createElementParserField(SimpleJsonTypes.INTEGER_PARSER);
        } else if (type.equals(Types.Boxed.DOUBLE)) {
            field = createElementParserField(SimpleJsonTypes.DOUBLE_PARSER);
        } else if (type.equals(Types.Boxed.LONG)) {
            field = createElementParserField(SimpleJsonTypes.LONG_PARSER);
        } else if (type.equals(Types.Boxed.BOOLEAN)) {
            field = createElementParserField(SimpleJsonTypes.BOOLEAN_PARSER);
        } else {
            final TypeElement element = type.getTypeElement();
            if (element == null) {
                mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not find a parser for " + type.className() + "!!1", mInterfaceType);
                throw new IllegalStateException("Could not find a parser for " + type.className() + "!!1");
            } else {
                if (Utils.hasAnnotation(element, Annotations.JSON_ENTITY)) {
                    final Type parserType = SimpleJsonTypes.ENTITY_PARSER.genericVersion(type);
                    field = mBuilder.addField(parserType, EnumSet.of(Modifier.PRIVATE, Modifier.FINAL));
                    field.setInitialValue(parserType.newInstance(type.className() + ".class"));
                } else {
                    mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not find a parser for " + type.className() + "!!1 Have you forgot to annotate it?", mInterfaceType);
                    throw new IllegalStateException("Could not find a parser for " + type.className() + "!!1 Have you forgot to annotate it?");
                }
            }
        }

        mParserMap.put(key, field);

        return field;
    }

    private Field createElementParserField(Type type) {
        final Field field = mBuilder.addField(type, EnumSet.of(Modifier.PRIVATE, Modifier.FINAL));
        field.setInitialValue(type.newInstance(new String[0]));
        return field;
    }
}
