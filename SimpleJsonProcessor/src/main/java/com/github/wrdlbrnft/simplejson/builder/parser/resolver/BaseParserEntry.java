package com.github.wrdlbrnft.simplejson.builder.parser.resolver;

import com.github.wrdlbrnft.codebuilder.code.CodeElement;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.simplejson.builder.ParserBuilder;
import com.github.wrdlbrnft.simplejson.builder.implementation.MappedValue;

import java.util.EnumSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 03/02/2018
 */

abstract class BaseParserEntry implements ElementParserResolver.ParserEntry {

    protected final ProcessingEnvironment mProcessingEnvironment;
    protected final ParserBuilder.BuildCache mBuildCache;

    BaseParserEntry(ProcessingEnvironment processingEnvironment, ParserBuilder.BuildCache buildCache) {
        mProcessingEnvironment = processingEnvironment;
        mBuildCache = buildCache;
    }

    @Override
    public final boolean matches(MappedValue value) {
        final TypeMirror type = value.getItemType();
        return matches(value, type);
    }

    @Override
    public final String getKey(MappedValue value) {
        final TypeMirror type = value.getItemType();
        return getKey(value, type);
    }

    @Override
    public final Field createField(MappedValue value) {
        final TypeMirror type = value.getItemType();
        return createField(value, type);
    }

    protected abstract boolean matches(MappedValue value, TypeMirror type);
    protected abstract String getKey(MappedValue value, TypeMirror type);
    protected abstract Field createField(MappedValue value, TypeMirror type);

    protected Field createElementParserField(Type baseType, Type implType, CodeElement... parameters) {
        return new Field.Builder()
                .setType(baseType)
                .setModifiers(EnumSet.of(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC))
                .setInitialValue(implType.newInstance(parameters))
                .build();
    }
}
