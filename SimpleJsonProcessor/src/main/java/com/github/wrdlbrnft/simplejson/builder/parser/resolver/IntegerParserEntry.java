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

class IntegerParserEntry extends BaseParserEntry {

    IntegerParserEntry(ProcessingEnvironment processingEnvironment, ParserBuilder.BuildCache buildCache) {
        super(processingEnvironment, buildCache);
    }

    @Override
    protected boolean matches(MappedValue value, TypeMirror type) {
        return Utils.isSameType(type, int.class) || Utils.isSameType(type, Integer.class);
    }

    @Override
    protected String getKey(MappedValue value, TypeMirror type) {
        return "_int";
    }

    @Override
    protected Field createField(MappedValue value, TypeMirror type) {
        return createElementParserField(
                Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.Boxed.INTEGER),
                SimpleJsonTypes.INTEGER_PARSER
        );
    }
}
