package com.github.wrdlbrnft.simplejson.builder.enums;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.BlockWriter;
import com.github.wrdlbrnft.codebuilder.elements.switches.Switch;
import com.github.wrdlbrnft.codebuilder.elements.values.Value;
import com.github.wrdlbrnft.codebuilder.elements.values.Values;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.codebuilder.variables.Variables;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;

/**
 * Created by kapeller on 09/07/15.
 */
class FormatBuilder extends ExecutableBuilder {

    private final Type mEnumType;
    private final EnumAnalyzerResult mResult;

    private Variable paramInput;

    FormatBuilder(Type enumType, EnumAnalyzerResult result) {
        mEnumType = enumType;
        mResult = result;
    }

    @Override
    protected List<Variable> createParameters() {
        final List<Variable> parameters = new ArrayList<>();

        parameters.add(paramInput = Variables.of(mEnumType));

        return parameters;
    }

    @Override
    protected void write(Block block) {

        final Switch.Builder switchBuilder = new Switch.Builder();
        switchBuilder.setVariable(paramInput);

        final Map<Element, Value> elementValueMap = mResult.getElementValueMap();
        for (Element element : elementValueMap.keySet()) {
            final Value value = elementValueMap.get(element);
            switchBuilder.addCase(Values.literal(element.getSimpleName()), new BlockWriter() {
                @Override
                protected void write(Block block) {
                    block.append("return ").append(value).append(";");
                }
            });
        }

        switchBuilder.setDefaultCase(new BlockWriter() {
            @Override
            protected void write(Block block) {
                final Element defaultElement = mResult.getDefaultElement();
                if (defaultElement != null) {
                    final Value value = elementValueMap.get(defaultElement);
                    block.append("return ").append(value).append(";");
                } else {
                    block.append("throw ").append(SimpleJsonTypes.SIMPLE_JSON_EXCEPTION.newInstance(new Block().append(Values.of("Could not map value ")).append(" + ").append(paramInput).append(" + ").append(Values.of("!!1"))));
                    block.append(";");
                }
            }
        });

        block.append(switchBuilder.build());
    }
}
