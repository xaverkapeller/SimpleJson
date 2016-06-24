package com.github.wrdlbrnft.simplejson.builder.retrofit;

import com.github.wrdlbrnft.codebuilder.annotations.Annotations;
import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.SourceFile;
import com.github.wrdlbrnft.codebuilder.elements.values.Values;
import com.github.wrdlbrnft.codebuilder.executables.Constructor;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.implementations.Implementation;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.TypeParameter;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.codebuilder.variables.Variables;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.parser.InternalParserBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

/**
 * Created by kapeller on 21/06/16.
 */

class MultiItemRequestConverterBuilder extends BaseRequestConverterBuilder {

    private static final String CLASS_NAME = "SimpleJsonMultiItemRequestBodyConverter";
    private final ProcessingEnvironment mProcessingEnvironment;
    private final Type mConverterType;
    private final Type mMediaTypeType;
    private final Type mRequestBodyType;

    public MultiItemRequestConverterBuilder(ProcessingEnvironment processingEnvironment, Type converterType, Type mediaTypeType, Type requestBodyType) {
        mProcessingEnvironment = processingEnvironment;
        mConverterType = converterType;
        mMediaTypeType = mediaTypeType;
        mRequestBodyType = requestBodyType;
    }

    @Override
    public Type build(String packageName) throws IOException {
        final TypeParameter typeParameter = Types.randomTypeParameter();
        final Type parserType = Types.generic(SimpleJsonTypes.PARSER, typeParameter);

        final Field parserField = new Field.Builder()
                .setType(parserType)
                .setModifiers(EnumSet.of(Modifier.PRIVATE, Modifier.FINAL))
                .build();

        final Field mediaTypeField = new Field.Builder()
                .setModifiers(EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL))
                .setType(mMediaTypeType)
                .setInitialValue(METHOD_MEDIA_TYPE_PARSE.callOnTarget(mMediaTypeType, Values.of(MEDIA_TYPE_JSON)))
                .build();

        final Implementation implementation = new Implementation.Builder()
                .setName(CLASS_NAME)
                .addField(parserField)
                .addField(mediaTypeField)
                .addTypeParameter(typeParameter)
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL))
                .addImplementedType(Types.generic(mConverterType, Types.generic(Types.LIST, typeParameter), mRequestBodyType))
                .addConstructor(new Constructor.Builder()
                        .setModifiers(EnumSet.of(Modifier.PUBLIC))
                        .setCode(new ExecutableBuilder() {

                            private Variable paramParser;

                            @Override
                            protected List<Variable> createParameters() {
                                final List<Variable> parameters = new ArrayList<>();
                                parameters.add(paramParser = Variables.of(parserType));
                                return parameters;
                            }

                            @Override
                            protected void write(Block block) {
                                block.set(parserField, paramParser).append(";");
                            }
                        })
                        .build())
                .addMethod(new Method.Builder()
                        .setName("convert")
                        .setModifiers(EnumSet.of(Modifier.PUBLIC))
                        .setReturnType(mRequestBodyType)
                        .addAnnotation(Annotations.forType(Override.class))
                        .addThrownException(Types.Exceptions.IO_EXCEPTION)
                        .setCode(new ExecutableBuilder() {

                            private Variable paramEntity;

                            @Override
                            protected List<Variable> createParameters() {
                                final List<Variable> parameters = new ArrayList<>();
                                parameters.add(paramEntity = Variables.of(Types.generic(Types.LIST, typeParameter)));
                                return parameters;
                            }

                            @Override
                            protected void write(Block block) {
                                final Variable varJson = Variables.of(Types.STRING, Modifier.FINAL);
                                block.set(varJson, InternalParserBuilder.METHOD_STUB_LIST_TO_JSON.callOnTarget(parserField, paramEntity)).append(";").newLine();
                                block.append("return ").append(METHOD_REQUEST_BODY_CREATE.callOnTarget(mRequestBodyType, mediaTypeField, varJson)).append(";");
                            }
                        })
                        .build())
                .build();

        final SourceFile sourceFile = SourceFile.create(mProcessingEnvironment, packageName);
        final Type type = sourceFile.write(implementation);
        sourceFile.flushAndClose();
        return type;
    }
}
