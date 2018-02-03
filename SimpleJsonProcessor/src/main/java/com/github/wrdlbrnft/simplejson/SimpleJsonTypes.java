package com.github.wrdlbrnft.simplejson;


import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.executables.Methods;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;

/**
 * Created by kapeller on 21/04/15.
 */
public class SimpleJsonTypes {

    public static final Type BASE_PARSER = Types.of("com.github.wrdlbrnft.simplejson.parsers", "BaseParser");
    public static final Type PARSER = Types.of("com.github.wrdlbrnft.simplejson.parsers", "Parser");
    public static final Type ELEMENT_PARSER = Types.of("com.github.wrdlbrnft.simplejson.parsers", "ElementParser");
    public static final Type ENUM_PARSER = Types.of("com.github.wrdlbrnft.simplejson.parsers", "EnumParser");
    public static final Type BASE_ENUM_PARSER = Types.of("com.github.wrdlbrnft.simplejson.parsers", "BaseEnumParser");
    public static final Type STRING_PARSER = Types.of("com.github.wrdlbrnft.simplejson.parsers.base", "StringParser");
    public static final Type DATE_PARSER = Types.of("com.github.wrdlbrnft.simplejson.parsers.extensions", "DateParser");
    public static final Type CALENDAR_PARSER = Types.of("com.github.wrdlbrnft.simplejson.parsers.extensions", "CalendarParser");
    public static final Type INTEGER_PARSER = Types.of("com.github.wrdlbrnft.simplejson.parsers.base", "IntegerParser");
    public static final Type LONG_PARSER = Types.of("com.github.wrdlbrnft.simplejson.parsers.base", "LongParser");
    public static final Type DOUBLE_PARSER = Types.of("com.github.wrdlbrnft.simplejson.parsers.base", "DoubleParser");
    public static final Type FLOAT_PARSER = Types.of("com.github.wrdlbrnft.simplejson.parsers.base", "FloatParser");
    public static final Type BOOLEAN_PARSER = Types.of("com.github.wrdlbrnft.simplejson.parsers.base", "BooleanParser");
    public static final Type ENTITY_PARSER = Types.of("com.github.wrdlbrnft.simplejson.parsers.base", "EntityParser");

    public static final Type SIMPLE_JSON = Types.of("com.github.wrdlbrnft.simplejson", "SimpleJson");
    public static final Method SIMPLE_JSON_REGISTER_PARSER = Methods.stub("registerParser");

    public static final Type ENUM = Types.of("java.lang", "Enum");
    public static final Type JSON_OBJECT = Types.of("org.json", "JSONObject");
    public static final Type JSON_ARRAY = Types.of("org.json", "JSONArray");

    public static final Type JSON_EXCEPTION = Types.of("org.json", "JSONException");
    public static final Type SIMPLE_JSON_EXCEPTION = Types.of("com.github.wrdlbrnft.simplejson.exceptions", "SimpleJsonException");

    public static final Type COLLECTION = Types.of("java.util", "Collection");

    private SimpleJsonTypes() {

    }
}
