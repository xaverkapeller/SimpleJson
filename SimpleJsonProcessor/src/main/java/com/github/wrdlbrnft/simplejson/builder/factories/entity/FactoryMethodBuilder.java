package com.github.wrdlbrnft.simplejson.builder.factories.entity;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.CodeElement;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.simplejson.builder.implementation.ImplementationResult;
import com.github.wrdlbrnft.simplejson.builder.implementation.MappedValue;
import com.github.wrdlbrnft.simplejson.builder.implementation.MethodPairInfo;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.type.TypeMirror;

import static com.github.wrdlbrnft.simplejson.utils.ParameterUtils.formatAsParameterName;
import static com.github.wrdlbrnft.simplejson.utils.ParameterUtils.handleOptionalParameter;

/**
 * Created by kapeller on 09/07/15.
 */
class FactoryMethodBuilder extends ExecutableBuilder {

    private final List<CodeElement> mParameters = new ArrayList<>();
    private final ImplementationResult mResult;

    FactoryMethodBuilder(ImplementationResult result) {
        mResult = result;
    }

    @Override
    protected List<Variable> createParameters() {
        final List<Variable> parameters = new ArrayList<>();

        final List<MappedValue> mappedValues = mResult.getMappedValues();
        for (int i = 0, count = mappedValues.size(); i < count; i++) {
            final MappedValue mappedValue = mappedValues.get(i);
            final MethodPairInfo info = mappedValue.getMethodPairInfo();
            final TypeMirror typeMirror = info.getGetter().getReturnType();

            final Variable parameter = new Variable.Builder()
                    .setName(formatAsParameterName(info.getGroupName()))
                    .setType(Types.of(typeMirror))
                    .build();
            parameters.add(parameter);
            mParameters.add(handleOptionalParameter(mResult.getImplementationInfo(), mappedValue, parameter));
        }

        return parameters;
    }

    @Override
    protected void write(Block block) {
        final CodeElement[] parameters = mParameters.toArray(new CodeElement[0]);
        final Type implementationType = mResult.getImplType();
        block.append("return ").append(implementationType.newInstance(parameters)).append(";");
    }
}
