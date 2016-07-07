package com.github.wrdlbrnft.simplejson.builder.factories.enums;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.SourceFile;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.executables.Methods;
import com.github.wrdlbrnft.codebuilder.implementations.Implementation;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.codebuilder.variables.Variables;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.enums.EnumParserBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by kapeller on 24/04/15.
 */
public class EnumFactoryBuilder {

    private static final Method METHOD_PARSE = Methods.stub(EnumParserBuilder.METHOD_NAME_PARSE);
    private static final Method METHOD_FORMAT = Methods.stub(EnumParserBuilder.METHOD_NAME_FORMAT);

    private final ProcessingEnvironment mProcessingEnvironment;

    public EnumFactoryBuilder(ProcessingEnvironment processingEnvironment) {
        mProcessingEnvironment = processingEnvironment;
    }

    public void build(Type parserType, TypeElement enumElement) throws IOException {
        final Type enumType = Types.of(enumElement);

        final Implementation.Builder builder = new Implementation.Builder();
        builder.setName(createFactoryName(enumElement));
        builder.setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));

        final Field parserField = new Field.Builder()
                .setType(Types.generic(SimpleJsonTypes.ENUM_PARSER, enumType))
                .setModifiers(EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL))
                .setInitialValue(parserType.newInstance())
                .build();
        builder.addField(parserField);

        builder.addMethod(new Method.Builder()
                .setReturnType(enumType)
                .setName(EnumParserBuilder.METHOD_NAME_PARSE)
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .setCode(new ExecutableBuilder() {

                    private Variable varJson;

                    @Override
                    protected List<Variable> createParameters() {
                        final List<Variable> parameters = new ArrayList<>();
                        parameters.add(varJson = Variables.of(Types.STRING));
                        return parameters;
                    }

                    @Override
                    protected void write(Block block) {
                        block.append("return ").append(METHOD_PARSE.callOnTarget(parserField, varJson)).append(";");
                    }
                })
                .build());

        builder.addMethod(new Method.Builder()
                .setReturnType(Types.STRING)
                .setName(EnumParserBuilder.METHOD_NAME_FORMAT)
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .setCode(new ExecutableBuilder() {

                    private Variable varEnum;

                    @Override
                    protected List<Variable> createParameters() {
                        final List<Variable> parameters = new ArrayList<>();
                        parameters.add(varEnum = Variables.of(enumType));
                        return parameters;
                    }

                    @Override
                    protected void write(Block block) {
                        block.append("return ").append(METHOD_FORMAT.callOnTarget(parserField, varEnum)).append(";");
                    }
                })
                .build());

        final Implementation factoryImplementation = builder.build();

        final SourceFile sourceFile = SourceFile.create(mProcessingEnvironment, Utils.getPackageName(enumElement));
        sourceFile.write(factoryImplementation);
        sourceFile.flushAndClose();
    }

    private String createFactoryName(TypeElement element) {
        return Utils.getClassName(element).replace(".", "") + "Parser";
    }
}
