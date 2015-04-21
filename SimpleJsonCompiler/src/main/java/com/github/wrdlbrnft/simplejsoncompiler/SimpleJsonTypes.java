package com.github.wrdlbrnft.simplejsoncompiler;

import com.github.wrdlbrnft.codebuilder.elements.Type;
import com.github.wrdlbrnft.codebuilder.impl.Types;

import java.util.EnumSet;

import javax.lang.model.element.Modifier;

/**
 * Created by kapeller on 21/04/15.
 */
public class SimpleJsonTypes {

    public static final Type PARSER = Types.create("com.github.wrdlbrnft.simplejson", "Parser", EnumSet.of(Modifier.PUBLIC), null);
    public static final Type ELEMENT_PARSER = Types.create("com.github.wrdlbrnft.simplejson", "ElementParser", EnumSet.of(Modifier.PUBLIC), null);
    public static final Type STRING_PARSER = Types.create("com.github.wrdlbrnft.simplejson.parsers", "StringParser", EnumSet.of(Modifier.PUBLIC), ELEMENT_PARSER);
    public static final Type INTEGER_PARSER = Types.create("com.github.wrdlbrnft.simplejson.parsers", "IntegerParser", EnumSet.of(Modifier.PUBLIC), ELEMENT_PARSER);
    public static final Type LONG_PARSER = Types.create("com.github.wrdlbrnft.simplejson.parsers", "LongParser", EnumSet.of(Modifier.PUBLIC), ELEMENT_PARSER);
    public static final Type DOUBLE_PARSER = Types.create("com.github.wrdlbrnft.simplejson.parsers", "DoubleParser", EnumSet.of(Modifier.PUBLIC), ELEMENT_PARSER);
    public static final Type BOOLEAN_PARSER = Types.create("com.github.wrdlbrnft.simplejson.parsers", "BooleanParser", EnumSet.of(Modifier.PUBLIC), ELEMENT_PARSER);
    public static final Type ENTITY_PARSER = Types.create("com.github.wrdlbrnft.simplejson.parsers", "EntityParser", EnumSet.of(Modifier.PUBLIC), ELEMENT_PARSER);


    public static final Type ENUM = Types.create("java.lang", "Enum", EnumSet.of(Modifier.PUBLIC), Types.OBJECT);
    public static final Type JSON_OBJECT = Types.create("org.json", "JSONObject", EnumSet.of(Modifier.PUBLIC), Types.OBJECT);
    public static final Type JSON_ARRAY = Types.create("org.json", "JSONArray", EnumSet.of(Modifier.PUBLIC), Types.OBJECT);

    public static final Type THROWABLE = Types.create("java.lang", "Throwable", EnumSet.of(Modifier.PUBLIC), Types.OBJECT);
    public static final Type EXCEPTION = Types.create("java.lang", "Exception", EnumSet.of(Modifier.PUBLIC), THROWABLE);
    public static final Type JSON_EXCEPTION = Types.create("org.json", "JSONException", EnumSet.of(Modifier.PUBLIC), EXCEPTION);
}
