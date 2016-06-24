package com.github.wrdlbrnft.simplejson.builder.parser;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.elements.forloop.item.Foreach;
import com.github.wrdlbrnft.codebuilder.elements.values.Values;
import com.github.wrdlbrnft.codebuilder.executables.Methods;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.codebuilder.variables.Variables;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.implementation.MethodPairInfo;
import com.github.wrdlbrnft.simplejson.models.MappedValue;

import java.io.IOException;

import javax.lang.model.type.TypeMirror;

/**
 * Created by kapeller on 09/07/15.
 */
class EntityFormater {

    private final ElementParserResolver mParserResolver;

    public EntityFormater(ElementParserResolver parserResolver) {
        mParserResolver = parserResolver;
    }

    public void formatValue(Block block, MappedValue mappedValue, Variable varJsonObject, Variable varEntity) {
        final MethodPairInfo info = mappedValue.getMethodPairInfo();
        final TypeMirror type = mappedValue.getItemType();
        final Field parser = mParserResolver.getElementParserField(type);

        block.append(parser).append(".toJsonObject(")
                .append(varJsonObject).append(", ")
                .append(Values.of(mappedValue.getFieldName())).append(", ")
                .append(Methods.call(info.getGetter(), varEntity))
                .append(");").newLine();
    }

    public void formatList(Block block, MappedValue mappedValue, Variable varJsonObject, Variable varEntity) {
        formatCollection(block, mappedValue, varJsonObject, varEntity);
    }

    public void formatSet(Block block, MappedValue mappedValue, Variable varJsonObject, Variable varEntity) {
        formatCollection(block, mappedValue, varJsonObject, varEntity);
    }

    private void formatCollection(Block block, final MappedValue mappedValue, Variable varJsonObject, final Variable varEntity) {
        final MethodPairInfo info = mappedValue.getMethodPairInfo();
        final TypeMirror type = mappedValue.getItemType();
        final Field parser = mParserResolver.getElementParserField(type);
        final Variable varJsonArray = Variables.of(SimpleJsonTypes.JSON_ARRAY);
        block.set(varJsonArray, SimpleJsonTypes.JSON_ARRAY.newInstance()).append(";").newLine();

        block.append(new Foreach.Builder()
                .setItemType(Types.of(type))
                .setCollection(Methods.call(info.getGetter(), varEntity))
                .setIteration(new Foreach.Iteration() {
                    @Override
                    public void onIteration(Block block, Variable item) {
                        block.append(parser).append(".toJsonArray(").append(varJsonArray).append(", ").append(item).append(");");
                    }
                })
                .build());
        block.newLine();

        block.append(varJsonObject).append(".put(").append(Values.of(mappedValue.getFieldName())).append(", ").append(varJsonArray).append(");").newLine();
    }
}
