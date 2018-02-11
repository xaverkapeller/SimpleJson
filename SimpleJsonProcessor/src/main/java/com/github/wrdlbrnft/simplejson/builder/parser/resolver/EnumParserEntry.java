package com.github.wrdlbrnft.simplejson.builder.parser.resolver;

import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.simplejson.SimpleJsonAnnotations;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.ParserBuilder;
import com.github.wrdlbrnft.simplejson.builder.implementation.MappedValue;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 03/02/2018
 */

class EnumParserEntry extends BaseParserEntry {

    EnumParserEntry(ProcessingEnvironment processingEnvironment, ParserBuilder.BuildCache buildCache) {
        super(processingEnvironment, buildCache);
    }

    @Override
    protected boolean matches(MappedValue value, TypeMirror type) {
        final TypeElement element = (TypeElement) mProcessingEnvironment.getTypeUtils().asElement(type);
        return element != null && Utils.isSubTypeOf(mProcessingEnvironment, type, Enum.class) && Utils.hasAnnotation(element, SimpleJsonAnnotations.JSON_ENUM);
    }

    @Override
    protected String getKey(MappedValue value, TypeMirror type) {
        return "_enum_" + type;
    }

    @Override
    protected Field createField(MappedValue value, TypeMirror type) {
        final TypeElement element = (TypeElement) mProcessingEnvironment.getTypeUtils().asElement(type);
        return createElementParserField(
                Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.of(element)),
                mBuildCache.getEnumParser(element)
        );
    }
}
