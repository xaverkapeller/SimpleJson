package com.github.wrdlbrnft.simplejson.builder.parser.resolver;

import com.github.wrdlbrnft.codebuilder.code.CodeElement;
import com.github.wrdlbrnft.codebuilder.elements.values.Values;
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
                new CustomParserEntry(mProcessingEnvironment, mBuildCache),
                new EnumParserEntry(mProcessingEnvironment, mBuildCache),
                new FloatParserEntry(mProcessingEnvironment, mBuildCache),
                new IntegerParserEntry(mProcessingEnvironment, mBuildCache),
                new JsonEntityParserEntry(mProcessingEnvironment, mBuildCache),
                new LongParserEntry(mProcessingEnvironment, mBuildCache),
                new StringParserEntry(mProcessingEnvironment, mBuildCache)
        );
    }

    public CodeElement getElementParserField(MappedValue mappedValue) {
        final TypeMirror type = mappedValue.getItemType();
        final TypeElement element = (TypeElement) mProcessingEnvironment.getTypeUtils().asElement(type);

        if (mappedValue.isParentReference()) {
            return Values.ofThis();
        }

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
                .map(CodeElement.class::cast)
                .orElseGet(() -> {
                    mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to find a parser for " + type, mappedValue.getMethodPairInfo().getGetter());
                    return Values.ofNull();
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
