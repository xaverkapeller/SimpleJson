package com.github.wrdlbrnft.simplejson.builder.implementation;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.codebuilder.variables.Variables;
import com.github.wrdlbrnft.simplejson.models.MappedValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kapeller on 09/07/15.
 */
class ConstructorBuilder extends ExecutableBuilder {

    private final List<MappedValue> mMappedValues;
    private final Map<Field, Variable> parameterMap = new HashMap<>();

    ConstructorBuilder(List<MappedValue> mappedValues) {
        mMappedValues = mappedValues;
    }

    @Override
    protected List<Variable> createParameters() {
        final List<Variable> parameters = new ArrayList<>();

        for (int i = 0, count = mMappedValues.size(); i < count; i++) {
            final MappedValue mappedValue = mMappedValues.get(i);
            final Field field = mappedValue.getField();
            final Variable parameter = Variables.of(Types.of(mappedValue.getBaseType()));
            parameterMap.put(field, parameter);
            parameters.add(parameter);
        }

        return parameters;
    }

    @Override
    protected void write(Block block) {
        for (int i = 0, count = mMappedValues.size(); i < count; i++) {
            final MappedValue mappedValue = mMappedValues.get(i);
            final Field field = mappedValue.getField();
            final Variable parameter = parameterMap.get(field);

            if (i > 0) {
                block.newLine();
            }

            block.set(field, parameter).append(";");
        }
    }
}
