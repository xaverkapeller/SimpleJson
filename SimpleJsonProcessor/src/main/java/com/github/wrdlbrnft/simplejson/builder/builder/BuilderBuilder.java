package com.github.wrdlbrnft.simplejson.builder.builder;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.CodeBuilder;
import com.github.wrdlbrnft.codebuilder.code.CodeElement;
import com.github.wrdlbrnft.codebuilder.code.NameGenerator;
import com.github.wrdlbrnft.codebuilder.code.Resolver;
import com.github.wrdlbrnft.codebuilder.elements.values.Values;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.implementations.Implementation;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.simplejson.builder.implementation.MethodPairInfo;
import com.github.wrdlbrnft.simplejson.models.ImplementationResult;
import com.github.wrdlbrnft.simplejson.models.MappedValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 04/02/2018
 */

public class BuilderBuilder {

    private static final List<String> PARAMETER_NAME_BLACK_LIST = Arrays.asList(
            "int",
            "long",
            "float",
            "double",
            "boolean"
    );

    public Implementation build(ImplementationResult result) {
        final Implementation.Builder builder = new Implementation.Builder()
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .setName("Builder");

        final TypeStub builderType = new TypeStub();

        final List<CodeElement> fields = new ArrayList<>();

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
            fields.add(field);

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
                        final CodeElement instance = result.getImplType().newInstance(fields.toArray(new CodeElement[fields.size()]));
                        block.append("return ").append(instance).append(";");
                    }
                })
                .build());

        final Implementation implementation = builder.build();
        builderType.setType(implementation);
        return implementation;
    }

    private String formatAsParameterName(String groupName) {
        final String name = groupName.substring(0, 1).toLowerCase() + groupName.substring(1);
        if (PARAMETER_NAME_BLACK_LIST.contains(name)) {
            return "_" + name;
        }
        return name;
    }

    private static class TypeStub implements Type {

        private Type mType;

        public void setType(Type type) {
            mType = type;
        }

        @Override
        public CodeElement newInstance(CodeElement... codeElements) {
            return mType.newInstance(codeElements);
        }

        @Override
        public CodeElement classObject() {
            return mType.classObject();
        }

        @Override
        public void prepare() {
            mType.prepare();
        }

        @Override
        public void resolve(Resolver resolver, NameGenerator nameGenerator) {
            mType.resolve(resolver, nameGenerator);
        }

        @Override
        public void write(CodeBuilder codeBuilder) {
            mType.write(codeBuilder);
        }
    }
}
