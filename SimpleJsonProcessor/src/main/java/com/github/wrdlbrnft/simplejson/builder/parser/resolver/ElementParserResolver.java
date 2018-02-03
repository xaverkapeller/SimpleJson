package com.github.wrdlbrnft.simplejson.builder.parser.resolver;

import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.simplejson.SimpleJsonAnnotations;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.ParserBuilder;
import com.github.wrdlbrnft.simplejson.builder.implementation.MethodPairInfo;
import com.github.wrdlbrnft.simplejson.models.MappedValue;

import java.util.ArrayList;
import java.util.Arrays;
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
public class ElementParserResolver {

    public interface ParserEntry {
        boolean matches(MappedValue value);
        String getKey(MappedValue value);
        Field createField(MappedValue value);
    }

    private final Map<String, Field> mParserMap = new HashMap<>();
    private final List<Field> mFields = new ArrayList<>();

    private final List<ParserEntry> mEntries;

    private final ProcessingEnvironment mProcessingEnvironment;
    private final ParserBuilder.BuildCache mBuildCache;
    private final TypeElement mInterfaceType;

    public ElementParserResolver(ProcessingEnvironment processingEnvironment, TypeElement interfaceType, ParserBuilder.BuildCache buildCache) {
        mProcessingEnvironment = processingEnvironment;
        mInterfaceType = interfaceType;
        mBuildCache = buildCache;
        mEntries = Arrays.asList(
                new BooleanParserEntry(mProcessingEnvironment, mBuildCache),
                new CalendarParserEntry(mProcessingEnvironment, mBuildCache),
                new DateParserEntry(mProcessingEnvironment, mBuildCache),
                new DoubleParserEntry(mProcessingEnvironment, mBuildCache),
                new EnumParserEntry(mProcessingEnvironment, mBuildCache),
                new FloatParserEntry(mProcessingEnvironment, mBuildCache),
                new IntegerParserEntry(mProcessingEnvironment, mBuildCache),
                new JsonEntityParserEntry(mProcessingEnvironment, mBuildCache),
                new LongParserEntry(mProcessingEnvironment, mBuildCache),
                new StringParserEntry(mProcessingEnvironment, mBuildCache)
        );
    }

    public Field getElementParserField(MappedValue mappedValue) {
        final TypeMirror type = mappedValue.getItemType();
        final TypeElement element = (TypeElement) mProcessingEnvironment.getTypeUtils().asElement(type);

        return mEntries.stream()
                .filter(entry -> entry.matches(mappedValue))
                .findAny()
                .map(entry -> {
                    final String key = entry.getKey(mappedValue);
                    if (mParserMap.containsKey(key)) {
                        return mParserMap.get(key);
                    }
                    final Field field = entry.createField(mappedValue);
                    mParserMap.put(key, field);
                    mFields.add(field);
                    return field;
                })
                .orElseGet(() -> {
                    final MethodPairInfo methodPairInfo = mappedValue.getMethodPairInfo();
                    final AnnotationValue parserClassValue = methodPairInfo.findAnnotationValue(SimpleJsonAnnotations.FIELD_NAME, "parserClass");
                    if (parserClassValue != null) {
                        final TypeMirror parserTypeMirror = (TypeMirror) parserClassValue.getValue();
                        return createElementParserField(
                                Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.of(type)),
                                Types.of(parserTypeMirror)
                        );
                    } else {

                        final Type parser = mBuildCache.getCustomParser(element);
                        if (parser == null) {
                            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not find a parser for " + element.getSimpleName() + "!!1 Have you forgot to annotate it? If the class is a framework class then most likely it is not supported to be used in entities created with this library.", mInterfaceType);
                            return null;
                        }

                        return createElementParserField(
                                Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.of(type)),
                                parser
                        );
                    }
                });
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
