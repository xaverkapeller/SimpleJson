package com.github.wrdlbrnft.simplejson.builder.parser;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.codebuilder.variables.Variables;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.implementation.MappedValue;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by kapeller on 09/07/15.
 */
class ToJsonObjectBuilder extends ExecutableBuilder {

    private final TypeElement mInterfaceElement;
    private final List<MappedValue> mMappedValues;
    private final EntityFormater mEntityFormater;

    private Variable paramEntity;

    public ToJsonObjectBuilder(TypeElement interfaceElement, List<MappedValue> mappedValues, EntityFormater entityFormater) {
        mInterfaceElement = interfaceElement;
        mMappedValues = mappedValues;
        mEntityFormater = entityFormater;
    }

    @Override
    protected List<Variable> createParameters() {
        final List<Variable> parameters = new ArrayList<Variable>();

        parameters.add(paramEntity = Variables.of(Types.of(mInterfaceElement)));

        return parameters;
    }

    @Override
    protected void write(Block block) {
        final Variable varJsonObject = Variables.of(SimpleJsonTypes.JSON_OBJECT, Modifier.FINAL);
        block.set(varJsonObject, SimpleJsonTypes.JSON_OBJECT.newInstance()).append(";").newLine();

        for (int i = 0, count = mMappedValues.size(); i < count; i++) {
            final MappedValue mappedValue = mMappedValues.get(i);

            switch (mappedValue.getValueType()) {

                case VALUE:
                    mEntityFormater.formatValue(block, mappedValue, varJsonObject, paramEntity);
                    break;

                case LIST:
                    mEntityFormater.formatList(block, mappedValue, varJsonObject, paramEntity);
                    break;

                case SET:
                    mEntityFormater.formatSet(block, mappedValue, varJsonObject, paramEntity);
                    break;

                default:
                    throw new IllegalStateException("Encountered unknown ValueType! Compiler is broken...");
            }
        }

        block.append("return ").append(varJsonObject).append(";");
    }
}
