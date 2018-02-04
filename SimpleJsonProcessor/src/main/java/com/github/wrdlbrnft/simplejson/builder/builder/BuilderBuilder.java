package com.github.wrdlbrnft.simplejson.builder.builder;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.CodeElement;
import com.github.wrdlbrnft.codebuilder.elements.values.Values;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.implementations.Implementation;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.simplejson.builder.implementation.MethodPairInfo;
import com.github.wrdlbrnft.simplejson.models.ImplementationResult;
import com.github.wrdlbrnft.simplejson.models.MappedValue;
import com.github.wrdlbrnft.simplejson.utils.TypeStub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import static com.github.wrdlbrnft.simplejson.utils.ParameterUtils.formatAsParameterName;
import static com.github.wrdlbrnft.simplejson.utils.ParameterUtils.handleOptionalParameter;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 04/02/2018
 */

public class BuilderBuilder {

    public Implementation build(ImplementationResult result) {
        final Implementation.Builder builder = new Implementation.Builder()
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .setName("Builder");

        final TypeStub builderType = new TypeStub();

        final List<CodeElement> parameters = new ArrayList<>();

        for (MappedValue mappedValue : result.getMappedValues()) {
            final MethodPairInfo info = mappedValue.getMethodPairInfo();
            final TypeMirror typeMirror = info.getGetter().getReturnType();

            final Variable parameter = new Variable.Builder()
                    .setName(formatAsParameterName(info.getGroupName()))
                    .setType(Types.of(typeMirror))
                    .build();

            final Field field = new Field.Builder()
                    .setType(Types.of(typeMirror))
                    .setModifiers(EnumSet.of(Modifier.PRIVATE))
                    .build();

            parameters.add(handleOptionalParameter(mappedValue, field));

            builder.addField(field);
            builder.addMethod(new Method.Builder()
                    .setName("set" + info.getGroupName())
                    .setModifiers(EnumSet.of(Modifier.PUBLIC))
                    .setReturnType(builderType)
                    .setCode(new ExecutableBuilder() {
                        @Override
                        protected List<Variable> createParameters() {
                            return Collections.singletonList(parameter);
                        }

                        @Override
                        protected void write(Block block) {
                            block.set(field, parameter).append(";").newLine();
                            block.append("return ").append(Values.ofThis()).append(";");
                        }
                    })
                    .build());
        }

        builder.addMethod(new Method.Builder()
                .setName("build")
                .setModifiers(EnumSet.of(Modifier.PUBLIC))
                .setReturnType(Types.of(result.getInterfaceType()))
                .setCode(new ExecutableBuilder() {
                    @Override
                    protected List<Variable> createParameters() {
                        return Collections.emptyList();
                    }

                    @Override
                    protected void write(Block block) {
                        final CodeElement instance = result.getImplType().newInstance(parameters.toArray(new CodeElement[parameters.size()]));
                        block.append("return ").append(instance).append(";");
                    }
                })
                .build());

        final Implementation implementation = builder.build();
        builderType.setType(implementation);
        return implementation;
    }
}
