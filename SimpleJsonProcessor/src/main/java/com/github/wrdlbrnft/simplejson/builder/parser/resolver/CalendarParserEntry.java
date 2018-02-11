package com.github.wrdlbrnft.simplejson.builder.parser.resolver;

import com.github.wrdlbrnft.codebuilder.elements.values.Values;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.simplejson.SimpleJsonAnnotations;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.ParserBuilder;
import com.github.wrdlbrnft.simplejson.builder.implementation.MappedValue;

import java.util.Calendar;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 03/02/2018
 */

class CalendarParserEntry extends BaseParserEntry {

    CalendarParserEntry(ProcessingEnvironment processingEnvironment, ParserBuilder.BuildCache buildCache) {
        super(processingEnvironment, buildCache);
    }

    @Override
    protected boolean matches(MappedValue value, TypeMirror type) {
        return Utils.isSameType(type, Calendar.class);
    }

    @Override
    protected String getKey(MappedValue value, TypeMirror type) {
        final ExecutableElement getter = value.getMethodPairInfo().getGetter();
        if (Utils.hasAnnotation(getter, SimpleJsonAnnotations.DATE_PATTERN)) {
            final String pattern = (String) Utils.getAnnotationValue(getter, SimpleJsonAnnotations.DATE_PATTERN, "value").getValue();
            return "_calendar_pattern_" + pattern;
        } else if (Utils.hasAnnotation(getter, SimpleJsonAnnotations.UNIX_TIME_STAMP)) {
            final AnnotationValue annotationValue = Utils.getAnnotationValue(getter, SimpleJsonAnnotations.UNIX_TIME_STAMP, "inMilliSeconds");
            final boolean inMilliSeconds = annotationValue == null || (boolean) annotationValue.getValue();
            return "_calendar_unix_time_stamp_" + inMilliSeconds;
        } else {
            return "_calendar_unix_time_stamp_default";
        }
    }

    @Override
    protected Field createField(MappedValue value, TypeMirror type) {
        final ExecutableElement getter = value.getMethodPairInfo().getGetter();
        if (Utils.hasAnnotation(getter, SimpleJsonAnnotations.DATE_PATTERN)) {
            final String pattern = (String) Utils.getAnnotationValue(getter, SimpleJsonAnnotations.DATE_PATTERN, "value").getValue();
            return createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.CALENDAR),
                    SimpleJsonTypes.CALENDAR_PARSER,
                    SimpleJsonTypes.PATTERN_DATE_FORMATTER.newInstance(Values.of(pattern))
            );
        } else if (Utils.hasAnnotation(getter, SimpleJsonAnnotations.UNIX_TIME_STAMP)) {
            final AnnotationValue annotationValue = Utils.getAnnotationValue(getter, SimpleJsonAnnotations.UNIX_TIME_STAMP, "inMilliSeconds");
            final boolean inMilliSeconds = annotationValue == null || (boolean) annotationValue.getValue();
            return createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.CALENDAR),
                    SimpleJsonTypes.CALENDAR_PARSER,
                    SimpleJsonTypes.UNIX_TIME_STAMP_DATE_FORMATTER.newInstance(Values.of(inMilliSeconds))
            );
        } else {
            return createElementParserField(
                    Types.generic(SimpleJsonTypes.ELEMENT_PARSER, Types.CALENDAR),
                    SimpleJsonTypes.CALENDAR_PARSER,
                    SimpleJsonTypes.UNIX_TIME_STAMP_DATE_FORMATTER.newInstance(Values.of(true))
            );
        }
    }
}
