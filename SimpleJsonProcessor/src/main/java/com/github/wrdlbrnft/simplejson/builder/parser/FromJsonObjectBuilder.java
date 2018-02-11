package com.github.wrdlbrnft.simplejson.builder.parser;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.codebuilder.variables.Variables;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.implementation.MappedValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kapeller on 09/07/15.
 */
class FromJsonObjectBuilder extends ExecutableBuilder {

    private final EntityParser mEntityParser;
    private final List<MappedValue> mMappedValues;
    private final Type mImplementationType;

    private Variable paramJsonObject;

    FromJsonObjectBuilder(Type implementationType, List<MappedValue> mappedValues, EntityParser entityParser) {
        mEntityParser = entityParser;
        mMappedValues = mappedValues;
        mImplementationType = implementationType;
    }

    @Override
    protected List<Variable> createParameters() {
        final List<Variable> parameters = new ArrayList<>();

        parameters.add(paramJsonObject = Variables.of(SimpleJsonTypes.JSON_OBJECT));

        return parameters;
    }

    @Override
    protected void write(Block block) {
        final Variable[] params = new Variable[mMappedValues.size()];
        for (int i = 0, count = mMappedValues.size(); i < count; i++) {
            final MappedValue mappedValue = mMappedValues.get(i);

            switch (mappedValue.getValueType()) {

                case VALUE:
                    params[i] = mEntityParser.parseValue(block, mappedValue, paramJsonObject);
                    break;

                case LIST:
                    params[i] = mEntityParser.parseList(block, mappedValue, paramJsonObject);
                    break;

                case SET:
                    params[i] = mEntityParser.parseSet(block, mappedValue, paramJsonObject);
                    break;

                default:
                    throw new IllegalStateException("Encountered unknown ValueType! Compiler is broken...");
            }
        }

        block.append("return ").append(mImplementationType.newInstance(params)).append(";");
    }
}
