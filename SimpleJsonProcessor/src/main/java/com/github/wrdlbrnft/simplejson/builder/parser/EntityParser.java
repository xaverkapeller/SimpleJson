package com.github.wrdlbrnft.simplejson.builder.parser;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.BlockWriter;
import com.github.wrdlbrnft.codebuilder.code.CodeElement;
import com.github.wrdlbrnft.codebuilder.code.Statement;
import com.github.wrdlbrnft.codebuilder.elements.forloop.counting.CountingFor;
import com.github.wrdlbrnft.codebuilder.elements.ifs.If;
import com.github.wrdlbrnft.codebuilder.elements.values.Values;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Operators;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.codebuilder.variables.Variables;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.models.MappedValue;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/**
 * Created by kapeller on 09/07/15.
 */
class EntityParser {

    private final ElementParserResolver mElementParserResolver;

    public EntityParser(ElementParserResolver elementParserResolver) {
        mElementParserResolver = elementParserResolver;
    }

    public Variable parseValue(Block block, MappedValue mappedValue, final Variable varJsonObject) {
        final CodeElement key = Values.of(mappedValue.getFieldName());
        final TypeMirror type = mappedValue.getItemType();
        final Field parser = mElementParserResolver.getElementParserField(type);
        final Variable variable = Variables.of(Types.of(type), Modifier.FINAL);

        if (mappedValue.isOptional()) {
            block.set(variable, new BlockWriter() {
                @Override
                protected void write(Block block) {
                    block.append(varJsonObject).append(".has(").append(key).append(") ")
                            .append("? ").append(parser).append(".fromJsonObject(").append(varJsonObject).append(", ").append(key).append(") ")
                            .append(": ").append(Values.ofNull());
                }
            }).append(";");
        } else {
            block.set(variable, new BlockWriter() {
                @Override
                protected void write(Block block) {
                    block.append(parser).append(".fromJsonObject(").append(varJsonObject).append(", ").append(key).append(")");
                }
            }).append(";");
        }
        block.newLine();

        return variable;
    }

    public Variable parseList(Block block, final MappedValue mappedValue, final Variable varJsonObject) {
        final TypeMirror itemType = mappedValue.getItemType();
        final Field parser = mElementParserResolver.getElementParserField(itemType);
        final Variable varList = Variables.of(Types.generic(Types.LIST, Types.of(itemType)), Modifier.FINAL);
        block.set(varList, Types.generic(Types.ARRAY_LIST, Types.of(itemType)).newInstance()).append(";").newLine();

        handleOptionalAnnotation(block, mappedValue, varJsonObject, parser, varList);

        return varList;
    }


    public Variable parseSet(Block block, final MappedValue mappedValue, final Variable varJsonObject) {
        final TypeMirror itemType = mappedValue.getItemType();
        final Field parser = mElementParserResolver.getElementParserField(itemType);
        final Variable varSet = Variables.of(Types.generic(Types.SET, Types.of(itemType)), Modifier.FINAL);
        block.set(varSet, Types.generic(Types.HASH_SET, Types.of(itemType)).newInstance()).append(";").newLine();

        handleOptionalAnnotation(block, mappedValue, varJsonObject, parser, varSet);

        return varSet;
    }

    private void handleOptionalAnnotation(Block block, final MappedValue mappedValue, final Variable varJsonObject, final Field parser, final Variable varSet) {
        if (mappedValue.isOptional()) {
            final Block trueBlock = new Block();
            parseCollection(trueBlock, varSet, varJsonObject, mappedValue.getFieldName(), parser);

            block.append(new If.Builder()
                    .add(new Block().append(varJsonObject).append(".has(").append(Values.of(mappedValue.getFieldName())).append(")"), trueBlock)
                    .build());
            block.newLine();
        } else {
            parseCollection(block, varSet, varJsonObject, mappedValue.getFieldName(), parser);
        }
    }

    private void parseCollection(Block block, final Variable varCollection, Variable varJsonObject, String key, final Field parser) {
        final Variable varJsonArray = Variables.of(SimpleJsonTypes.JSON_ARRAY, Modifier.FINAL);

        block.set(varJsonArray, new Block().append(varJsonObject).append(".getJSONArray(").append(Values.of(key)).append(")")).append(";").newLine();
        block.append(new CountingFor.Builder()
                .setValues(Values.of(0), new Block().append(varJsonArray).append(".length()"))
                .setIteration(new CountingFor.Iteration() {
                    @Override
                    public void onIteration(Block block, Variable index, CodeElement endValue) {
                        block.append(varCollection).append(".add(").append(parser).append(".fromJsonArray(").append(varJsonArray).append(", ").append(index).append(")").append(");");
                    }

                    @Override
                    public void onCompare(Block block, Variable index, CodeElement endValue) {
                        block.append(Operators.operate(index, "<", endValue));
                    }
                })
                .build());
        block.newLine();
    }
}
