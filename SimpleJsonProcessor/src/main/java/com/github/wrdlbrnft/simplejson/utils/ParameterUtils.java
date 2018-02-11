package com.github.wrdlbrnft.simplejson.utils;

import com.github.wrdlbrnft.codebuilder.code.CodeElement;
import com.github.wrdlbrnft.codebuilder.elements.values.Value;
import com.github.wrdlbrnft.codebuilder.elements.values.Values;
import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.executables.Methods;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.simplejson.builder.implementation.ImplementationInfo;
import com.github.wrdlbrnft.simplejson.builder.implementation.MappedValue;

import java.util.Arrays;
import java.util.List;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 04/02/2018
 */

public class ParameterUtils {

    private static final List<String> PARAMETER_NAME_BLACK_LIST = Arrays.asList(
            "int",
            "long",
            "float",
            "double",
            "boolean"
    );

    private static final Type TYPE_OBJECTS = Types.of("java.util", "Objects");
    private static final Method METHOD_REQUIRE_NON_NULL = Methods.stub("requireNonNull");

    public static String formatAsParameterName(String groupName) {
        final String name = groupName.substring(0, 1).toLowerCase() + groupName.substring(1);
        if (PARAMETER_NAME_BLACK_LIST.contains(name)) {
            return "_" + name;
        }
        return name;
    }

    public static CodeElement handleOptionalParameter(ImplementationInfo info, MappedValue value, CodeElement parameter) {
        final String groupName = value.getMethodPairInfo().getGroupName();

        if (value.getValueType() == MappedValue.ValueType.VALUE && Utils.isPrimitive(value.getItemType())) {
            return parameter;
        }

        if (info.isStrict()) {
            final Value nullErrorMessage = Values.of(groupName + " is not optional, you have to set a non null value for it.");
            return value.isOptional()
                    ? parameter
                    : METHOD_REQUIRE_NON_NULL.callOnTarget(TYPE_OBJECTS, parameter, nullErrorMessage);
        }

        return parameter;
    }
}
