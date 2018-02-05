package com.github.wrdlbrnft.simplejson.builder.parser.resolver;

import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.ParserBuilder;
import com.github.wrdlbrnft.simplejson.models.MappedValue;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 03/02/2018
 */

class CustomParserEntry extends BaseParserEntry {

    CustomParserEntry(ProcessingEnvironment processingEnvironment, ParserBuilder.BuildCache buildCache) {
        super(processingEnvironment, buildCache);
    }

    @Override
    protected boolean matches(MappedValue value, TypeMirror type) {
        final MethodPairInfo methodPairInfo = value.getMethodPairInfo();
        final AnnotationValue parserClassValue = methodPairInfo.findAnnotationValue(SimpleJsonAnnotations.FIELD_NAME, "parserClass");
        return parserClassValue != null;
    }

    @Override
    protected String getKey(MappedValue value, TypeMirror type) {
        final MethodPairInfo methodPairInfo = value.getMethodPairInfo();
        final AnnotationValue parserClassValue = methodPairInfo.findAnnotationValue(SimpleJsonAnnotations.FIELD_NAME, "parserClass");
        return "_custom_" + parserClassValue.getValue();
    }

    @Override
    protected Field createField(MappedValue value, TypeMirror type) {
        final MethodPairInfo methodPairInfo = value.getMethodPairInfo();
        final AnnotationValue parserClassValue = methodPairInfo.findAnnotationValue(SimpleJsonAnnotations.FIELD_NAME, "parserClass");
        return createElementParserField(
                Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.of(type)),
                Types.of(parserTypeMirror)
        );
    }
}
