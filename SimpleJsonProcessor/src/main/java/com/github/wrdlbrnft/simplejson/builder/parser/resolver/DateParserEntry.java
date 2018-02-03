package com.github.wrdlbrnft.simplejson.builder.parser.resolver;

import com.github.wrdlbrnft.codebuilder.elements.values.Values;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.simplejson.SimpleJsonAnnotations;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.ParserBuilder;
import com.github.wrdlbrnft.simplejson.models.MappedValue;

import java.util.Date;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 03/02/2018
 */

class DateParserEntry extends BaseParserEntry {

    DateParserEntry(ProcessingEnvironment processingEnvironment, ParserBuilder.BuildCache buildCache) {
        super(processingEnvironment, buildCache);
    }

    @Override
    protected boolean matches(MappedValue value, TypeMirror type) {
        return Utils.isSameType(type, Date.class);
    }

    @Override
    protected String getKey(MappedValue value, TypeMirror type) {
        final ExecutableElement getter = value.getMethodPairInfo().getGetter();
        if (Utils.hasAnnotation(getter, SimpleJsonAnnotations.DATE_PATTERN)) {
            final String pattern = (String) Utils.getAnnotationValue(getter, SimpleJsonAnnotations.DATE_PATTERN, "value").getValue();
            return "_date_pattern_" + pattern;
        } else if (Utils.hasAnnotation(getter, SimpleJsonAnnotations.UNIX_TIME_STAMP)) {
            final boolean inMilliSeconds = (boolean) Utils.getAnnotationValue(getter, SimpleJsonAnnotations.UNIX_TIME_STAMP, "inMilliSeconds").getValue();
            return "_date_unix_time_stamp_" + inMilliSeconds;
        } else {
            return "_date_unix_time_stamp_default";
        }
    }

    @Override
    protected Field createField(MappedValue value, TypeMirror type) {
        final ExecutableElement getter = value.getMethodPairInfo().getGetter();
        if (Utils.hasAnnotation(getter, SimpleJsonAnnotations.DATE_PATTERN)) {
            final String pattern = (String) Utils.getAnnotationValue(getter, SimpleJsonAnnotations.DATE_PATTERN, "value").getValue();
            return createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.DATE),
                    SimpleJsonTypes.DATE_PARSER,
                    SimpleJsonTypes.PATTERN_DATE_FORMATTER.newInstance(Values.of(pattern))
            );
        } else if (Utils.hasAnnotation(getter, SimpleJsonAnnotations.UNIX_TIME_STAMP)) {
            final boolean inMilliSeconds = (boolean) Utils.getAnnotationValue(getter, SimpleJsonAnnotations.UNIX_TIME_STAMP, "inMilliSeconds").getValue();
            return createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.DATE),
                    SimpleJsonTypes.DATE_PARSER,
                    SimpleJsonTypes.UNIX_TIME_STAMP_DATE_FORMATTER.newInstance(Values.of(inMilliSeconds))
            );
        } else {
            return createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.DATE),
                    SimpleJsonTypes.DATE_PARSER,
                    SimpleJsonTypes.UNIX_TIME_STAMP_DATE_FORMATTER.newInstance(Values.of(true))
            );
        }
    }
}
