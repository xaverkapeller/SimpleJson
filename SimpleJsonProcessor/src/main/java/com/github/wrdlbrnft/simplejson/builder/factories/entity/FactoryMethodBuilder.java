package com.github.wrdlbrnft.simplejson.builder.factories.entity;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.CodeElement;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.executables.Methods;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.simplejson.builder.implementation.MethodPairInfo;
import com.github.wrdlbrnft.simplejson.models.MappedValue;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.type.TypeMirror;

import static com.github.wrdlbrnft.simplejson.utils.ParameterUtils.formatAsParameterName;
import static com.github.wrdlbrnft.simplejson.utils.ParameterUtils.handleOptionalParameter;

/**
 * Created by kapeller on 09/07/15.
 */
class FactoryMethodBuilder extends ExecutableBuilder {

    private static final Type TYPE_OBJECTS = Types.of("java.util", "Objects");
    private static final Method METHOD_REQUIRE_NON_NULL = Methods.stub("requireNonNull");

    private final List<MappedValue> mMappedValues;
    private final Type mImplementationType;

    private final CodeElement[] mParameters;

    FactoryMethodBuilder(Type implementationType, List<MappedValue> mappedValues) {
        mMappedValues = mappedValues;
        mImplementationType = implementationType;
        mParameters = new CodeElement[mappedValues.size()];
    }

    @Override
    protected List<Variable> createParameters() {
        final List<Variable> parameters = new ArrayList<>();

        for (int i = 0, count = mMappedValues.size(); i < count; i++) {
            final MappedValue mappedValue = mMappedValues.get(i);
            final MethodPairInfo info = mappedValue.getMethodPairInfo();
            final TypeMirror typeMirror = info.getGetter().getReturnType();

            final Variable parameter = new Variable.Builder()
                    .setName(formatAsParameterName(info.getGroupName()))
                    .setType(Types.of(typeMirror))
                    .build();
            parameters.add(parameter);
            mParameters[i] = handleOptionalParameter(mappedValue, parameter);
        }

        return parameters;
    }

    @Override
    protected void write(Block block) {
        block.append("return ").append(mImplementationType.newInstance(mParameters)).append(";");
    }
}
