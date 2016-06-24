package com.github.wrdlbrnft.simplejson.builder.factories.entity;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.simplejson.builder.implementation.MethodPairInfo;
import com.github.wrdlbrnft.simplejson.models.MappedValue;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.type.TypeMirror;

/**
 * Created by kapeller on 09/07/15.
 */
class FactoryMethodBuilder extends ExecutableBuilder {

    private final List<MappedValue> mMappedValues;
    private final Type mImplementationType;

    private final Variable[] mParameters;

    FactoryMethodBuilder(Type implementationType, List<MappedValue> mappedValues) {
        mMappedValues = mappedValues;
        mImplementationType = implementationType;
        mParameters = new Variable[mappedValues.size()];
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
            mParameters[i] = parameter;
        }

        return parameters;
    }

    @Override
    protected void write(Block block) {
        block.append("return ").append(mImplementationType.newInstance(mParameters)).append(";");
    }

    private String formatAsParameterName(String groupName) {
        return groupName.substring(0, 1).toLowerCase() + groupName.substring(1);
    }
}
