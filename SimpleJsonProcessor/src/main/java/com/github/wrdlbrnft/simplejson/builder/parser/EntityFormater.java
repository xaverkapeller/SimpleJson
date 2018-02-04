package com.github.wrdlbrnft.simplejson.builder.parser;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.BlockWriter;
import com.github.wrdlbrnft.codebuilder.code.CodeElement;
import com.github.wrdlbrnft.codebuilder.elements.forloop.item.Foreach;
import com.github.wrdlbrnft.codebuilder.elements.ifs.If;
import com.github.wrdlbrnft.codebuilder.elements.values.Values;
import com.github.wrdlbrnft.codebuilder.executables.Methods;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Operators;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.codebuilder.variables.Variables;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.implementation.MethodPairInfo;
import com.github.wrdlbrnft.simplejson.builder.parser.resolver.ElementParserResolver;
import com.github.wrdlbrnft.simplejson.models.MappedValue;

import javax.lang.model.element.Modifier;
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
        final CodeElement parser = mParserResolver.getElementParserField(mappedValue);

        if (mappedValue.isOptional()) {
            final Variable item = Variables.of(Types.of(info.getGetter().getReturnType()), Modifier.FINAL);
            block.set(item, Methods.call(info.getGetter(), varEntity)).append(";").newLine();

            block.append(new If.Builder()
                    .add(Operators.operate(item, "==", Values.ofNull()), new BlockWriter() {
                        @Override
                        protected void write(Block block) {
                            block.append(Methods.stub("put").callOnTarget(varJsonObject, Values.of(mappedValue.getFieldName()), Values.ofNull())).append(";");
                        }
                    })
                    .setElse(new BlockWriter() {
                        @Override
                        protected void write(Block block) {
                            block.append(parser).append(".toJsonObject(")
                                    .append(varJsonObject).append(", ")
                                    .append(Values.of(mappedValue.getFieldName())).append(", ")
                                    .append(item)
                                    .append(");");
                        }
                    })
                    .build());
            block.newLine();
        } else {
            block.append(parser).append(".toJsonObject(")
                    .append(varJsonObject).append(", ")
                    .append(Values.of(mappedValue.getFieldName())).append(", ")
                    .append(Methods.call(info.getGetter(), varEntity))
                    .append(");").newLine();
        }
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
        final CodeElement parser = mParserResolver.getElementParserField(mappedValue);
        final Variable varJsonArray = Variables.of(SimpleJsonTypes.JSON_ARRAY, Modifier.FINAL);
        block.set(varJsonArray, SimpleJsonTypes.JSON_ARRAY.newInstance()).append(";").newLine();

        final Variable collection = Variables.of(Types.of(info.getGetter().getReturnType()), Modifier.FINAL);
        block.set(collection, Methods.call(info.getGetter(), varEntity)).append(";").newLine();
        if(mappedValue.isOptional()) {
            block.append(new If.Builder()
                    .add(Operators.operate(collection, "==", Values.ofNull()), new BlockWriter() {
                        @Override
                        protected void write(Block block) {
                            block.append(Methods.stub("put").callOnTarget(varJsonObject, Values.of(mappedValue.getFieldName()), Values.ofNull())).append(";");
                        }
                    })
                    .setElse(new BlockWriter() {
                        @Override
                        protected void write(Block block) {
                            block.append(new Foreach.Builder()
                                    .setItemType(Types.of(type))
                                    .setCollection(collection)
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
                    })
                    .build());
            block.newLine();
        } else {
            block.append(new Foreach.Builder()
                    .setItemType(Types.of(type))
                    .setCollection(collection)
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
}
