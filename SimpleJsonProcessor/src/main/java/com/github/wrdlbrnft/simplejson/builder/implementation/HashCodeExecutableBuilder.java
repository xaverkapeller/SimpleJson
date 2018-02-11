package com.github.wrdlbrnft.simplejson.builder.implementation;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.BlockWriter;
import com.github.wrdlbrnft.codebuilder.code.CodeElement;
import com.github.wrdlbrnft.codebuilder.elements.ifs.TernaryIf;
import com.github.wrdlbrnft.codebuilder.elements.values.Values;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.executables.Methods;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Operators;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.codebuilder.variables.Variables;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/**
 * Created by kapeller on 22/06/16.
 */
class HashCodeExecutableBuilder extends ExecutableBuilder {

    private static final Method METHOD_DOUBLE_TO_LONG_BITS = Methods.stub("doubleToLongBits");
    private static final Method METHOD_FLOAT_TO_INT_BITS = Methods.stub("floatToIntBits");

    private final List<MappedValue> mMappedValues;

    HashCodeExecutableBuilder(List<MappedValue> mappedValues) {
        mMappedValues = mappedValues;
    }

    @Override
    protected List<Variable> createParameters() {
        final List<Variable> parameters = new ArrayList<>();
        return parameters;
    }

    @Override
    protected void write(Block block) {
        final Variable varResult = Variables.of(Types.Primitives.INTEGER);

        for (int i = 0, count = mMappedValues.size(); i < count; i++) {
            final MappedValue mappedValue = mMappedValues.get(i);
            final HashCodeStatement statement = createHashCodeStatement(mappedValue);
            final CodeElement setup = statement.getSetup();
            
            if(setup != null) {
                block.append(setup).newLine();
            }
            
            if (i > 0) {
                block.set(varResult, Operators.operate(
                        Operators.operate(Values.of(31), "*", varResult),
                        "+",
                        statement.getValue()
                ));
            } else {
                block.set(varResult, statement.getValue());
            }
            block.append(";").newLine();
        }

        block.append("return ").append(varResult).append(";");
    }

    private HashCodeStatement createHashCodeStatement(MappedValue mappedValue) {
        final Field field = mappedValue.getField();
        final TypeMirror type = mappedValue.getBaseType();

        if (Utils.isSameType(type, int.class)) {
            return new HashCodeStatement(null, field);
        }

        if (Utils.isSameType(type, long.class)) {
            return new HashCodeStatement(null, new LongToIntegerHashConversion(field));
        }

        if (Utils.isSameType(type, double.class)) {
            final Variable temp = Variables.of(Types.Primitives.LONG, Modifier.FINAL);
            final CodeElement setup = new BlockWriter() {
                @Override
                protected void write(Block block) {
                    block.set(temp, METHOD_DOUBLE_TO_LONG_BITS.callOnTarget(Types.Boxed.DOUBLE, field)).append(";");
                }
            };
            final CodeElement value = new BlockWriter() {
                @Override
                protected void write(Block block) {
                    block.append(new LongToIntegerHashConversion(temp));
                }
            };
            return new HashCodeStatement(setup, value);
        }

        if (Utils.isSameType(type, float.class)) {
            return new HashCodeStatement(null, new BracedStatement(new TernaryIf.Builder()
                    .setComparison(Operators.operate(field, "!=", new Block().append("+0.0f")))
                    .setTrueBlock(METHOD_FLOAT_TO_INT_BITS.callOnTarget(Types.Boxed.FLOAT, field))
                    .setFalseBlock(Values.of(0))
                    .build()));
        }

        if (Utils.isSameType(type, boolean.class)) {
            return new HashCodeStatement(null, new BracedStatement(new TernaryIf.Builder()
                    .setComparison(field)
                    .setTrueBlock(Values.of(1))
                    .setFalseBlock(Values.of(0))
                    .build()));
        }

        return new HashCodeStatement(null, new BracedStatement(new TernaryIf.Builder()
                .setComparison(Operators.operate(field, "!=", Values.ofNull()))
                .setTrueBlock(Methods.HASH_CODE.callOnTarget(field))
                .setFalseBlock(Values.of(0))
                .build()));
    }

    private static class LongToIntegerHashConversion extends BlockWriter {

        private final Variable mLong;

        private LongToIntegerHashConversion(Variable aLong) {
            mLong = aLong;
        }

        @Override
        protected void write(Block block) {
            block.append(Types.asCast(Types.Primitives.INTEGER)).append(" (")
                    .append(Operators.operate(mLong, "^", new BracedStatement(Operators.operate(mLong, ">>>", Values.of(32)))))
                    .append(")");
        }
    }

    private static class BracedStatement extends BlockWriter {

        private final CodeElement mStatement;

        private BracedStatement(CodeElement statement) {
            mStatement = statement;
        }

        @Override
        protected void write(Block block) {
            block.append("(").append(mStatement).append(")");
        }
    }
    
    private static class HashCodeStatement {
    
        private final CodeElement mSetup;
        private final CodeElement mHashCodeValue;
        
        public HashCodeStatement(CodeElement setup, CodeElement hashCodeValue) {
            mSetup = setup;
            mHashCodeValue = hashCodeValue;
        }
        
        public CodeElement getSetup() {
            return mSetup;
        }
        
        public CodeElement getValue() {
            return mHashCodeValue;
        }
    }
}
