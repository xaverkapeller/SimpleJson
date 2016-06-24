package com.github.wrdlbrnft.simplejson.builder.retrofit;

import com.github.wrdlbrnft.codebuilder.annotations.Annotations;
import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.SourceFile;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.executables.Methods;
import com.github.wrdlbrnft.codebuilder.implementations.Implementation;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.codebuilder.variables.Variables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

/**
 * Created by kapeller on 21/06/16.
 */
class SingleBitmapResponseConverterBuilder extends BaseResponseConverterBuilder {

    private static final Method METHOD_RESPONSE_BODY_BYTE_STREAM = Methods.stub("byteStream");
    private static final Method METHOD_BITMAP_FACTORY_DECODE_STREAM = Methods.stub("decodeStream");
    private static final String CLASS_NAME = "SimpleJsonBitmapResponseBodyConverter";

    private final ProcessingEnvironment mProcessingEnvironment;
    private final Type mConverterType;
    private final Type mBitmapType;
    private final Type mBitmapFactoryType;
    private final Type mResponseBodyType;

    public SingleBitmapResponseConverterBuilder(ProcessingEnvironment processingEnvironment, Type converterType, Type bitmapType, Type bitmapFactoryType, Type responseBodyType) {
        mProcessingEnvironment = processingEnvironment;
        mConverterType = converterType;
        mBitmapType = bitmapType;
        mBitmapFactoryType = bitmapFactoryType;
        mResponseBodyType = responseBodyType;
    }

    @Override
    public Type build(String packageName) throws IOException {
        final Implementation implementation = new Implementation.Builder()
                .setName(CLASS_NAME)
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL))
                .addImplementedType(Types.generic(mConverterType, mResponseBodyType, mBitmapType))
                .addMethod(new Method.Builder()
                        .setName("convert")
                        .setModifiers(EnumSet.of(Modifier.PUBLIC))
                        .setReturnType(mBitmapType)
                        .addAnnotation(Annotations.forType(Override.class))
                        .addThrownException(Types.Exceptions.IO_EXCEPTION)
                        .setCode(new ExecutableBuilder() {

                            private Variable paramResponseBody;

                            @Override
                            protected List<Variable> createParameters() {
                                final List<Variable> parameters = new ArrayList<>();
                                parameters.add(paramResponseBody = Variables.of(mResponseBodyType));
                                return parameters;
                            }

                            @Override
                            protected void write(Block block) {
                                block.append("return ").append(METHOD_BITMAP_FACTORY_DECODE_STREAM.callOnTarget(
                                        mBitmapFactoryType,
                                        METHOD_RESPONSE_BODY_BYTE_STREAM.callOnTarget(paramResponseBody)
                                )).append(";");
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
